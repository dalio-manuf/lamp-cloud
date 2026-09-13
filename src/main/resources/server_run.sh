#!/bin/bash

# ==============================================================================
# lamp-cloud 微服务服务端启停与持续部署管理脚本
# 用途: 配合 Jenkins CI/CD 自动部署或在部署服务器手动启停维护
# 语法: sh server_run.sh <应用名称> <环境标识> <执行动作>
# 示例: sh server_run.sh lamp-gateway-server prod restart
# ==============================================================================

# 尝试加载系统环境变量
[ -f /etc/profile ] && source /etc/profile
[ -f ~/.bash_profile ] && source ~/.bash_profile

# ------------------------------------------------------------------------------
# 入参解析
# ------------------------------------------------------------------------------
# 模块名称 (如: lamp-gateway-server, lamp-oauth-server, lamp-system-server)
RAW_JAR_NAME="$1"
# 环境标识 (如: dev, test, prod)
PROFILES="$2"
# 执行动作 (可选值: start | stop | restart | status | none)
ACTION="$3"

# 规范化应用名称 (去除可能携带的 .jar 后缀)
JAR_NAME="${RAW_JAR_NAME%.jar}"

# 工作空间根目录 (支持外部环境变量 WORKSPACE_HOME 覆盖)
WORKSPACE_HOME="${WORKSPACE_HOME:-/data_${PROFILES}}"

# ------------------------------------------------------------------------------
# 参数合法性校验
# ------------------------------------------------------------------------------
function printUsage() {
    echo -e "\033[0;31m[错误] 参数不完整或不符合规范！\033[0m"
    echo -e "\033[0;34m用法规范:\033[0m"
    echo -e "  sh $0 <应用名称> <环境标识> <执行动作>"
    echo -e "\033[0;34m参数说明:\033[0m"
    echo -e "  应用名称 : 如 lamp-gateway-server, lamp-oauth-server, lamp-system-server, lamp-base-server"
    echo -e "  环境标识 : 如 dev, test, prod"
    echo -e "  执行动作 : start | stop | restart | status | none"
    echo -e "\033[0;34m使用示例:\033[0m"
    echo -e "  sh $0 lamp-gateway-server prod start"
    echo -e "  sh $0 lamp-system-server prod restart"
    echo -e "  sh $0 lamp-oauth-server prod status"
}

if [ -z "${JAR_NAME}" ] || [ -z "${PROFILES}" ] || [ -z "${ACTION}" ]; then
    printUsage
    exit 1
fi

# 若动作为 none，代表 CI/CD 流程中无需在目标主机执行服务变更，直接退出并返回成功
if [ "${ACTION}" == "none" ]; then
    echo -e "\033[0;33m[跳过] 接收到动作指令为 none，无需在服务端执行启停操作。\033[0m"
    exit 0
fi

# 确保核心工作目录存在
mkdir -p "${WORKSPACE_HOME}/target"
mkdir -p "${WORKSPACE_HOME}/temp_jar"
mkdir -p "${WORKSPACE_HOME}/backups/${JAR_NAME}"
mkdir -p "${WORKSPACE_HOME}/logs"

TARGET_JAR="${WORKSPACE_HOME}/target/${JAR_NAME}.jar"
TEMP_JAR="${WORKSPACE_HOME}/temp_jar/${JAR_NAME}.jar"

# ------------------------------------------------------------------------------
# JVM 参数配置
# ------------------------------------------------------------------------------
JAVA_MEM_OPTS="${JAVA_MEM_OPTS:--server -Xms512M -Xmx1024M -Xss512k -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=512M -XX:+UseG1GC}"
JAVA_OPT="${JAVA_MEM_OPTS} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPT="${JAVA_OPT} -XX:HeapDumpPath=${WORKSPACE_HOME}/logs/heap_dump_${JAR_NAME}.hprof"
JAVA_OPT="${JAVA_OPT} -Dspring.profiles.active=${PROFILES} -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# SkyWalking APM Agent 探针 (按需启用)
# SKY_OPT="-javaagent:${WORKSPACE_HOME}/agent/skywalking-agent.jar -Dskywalking.agent.service_name=${JAR_NAME}"

# ------------------------------------------------------------------------------
# 辅助函数: 获取进程 PID
# ------------------------------------------------------------------------------
function get_pid() {
    ps -ef | grep -E "java.*${JAR_NAME}\.jar" | grep -v grep | awk '{print $2}'
}

# ------------------------------------------------------------------------------
# 辅助函数: 部署新 JAR (备份旧版本并覆盖更新)
# ------------------------------------------------------------------------------
function deploy_jar_if_present() {
    if [ -f "${TEMP_JAR}" ]; then
        echo -e "\033[0;36m[发布] 发现新构建的待发布包: ${TEMP_JAR}，准备执行备份与部署...\033[0m"
        
        # 1. 备份现有运行 JAR
        if [ -f "${TARGET_JAR}" ]; then
            TIME=$(date "+%Y%m%d%H%M%S")
            BACKUP_JAR="${WORKSPACE_HOME}/backups/${JAR_NAME}/${JAR_NAME}-${TIME}.jar"
            cp -f "${TARGET_JAR}" "${BACKUP_JAR}"
            echo -e "\033[0;32m[备份] 旧版本 JAR 已备份至: ${BACKUP_JAR}\033[0m"
        fi
        
        # 2. 覆盖至目标运行目录
        mv -f "${TEMP_JAR}" "${TARGET_JAR}"
        chmod 755 "${TARGET_JAR}"
        echo -e "\033[0;32m[更新] 新版本已就绪: ${TARGET_JAR}\033[0m"
    fi
}

# ------------------------------------------------------------------------------
# 服务操作函数
# ------------------------------------------------------------------------------
function start() {
    # 尝试更新新发布的 JAR
    deploy_jar_if_present

    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "\033[0;33m[提示] $JAR_NAME 正在运行中，PID: $pid\033[0m"
        return 0
    fi

    if [ ! -f "${TARGET_JAR}" ]; then
        echo -e "\033[0;31m[错误] 目标文件不存在: ${TARGET_JAR}\033[0m"
        return 1
    fi

    echo -e "\033[0;32m[启动] 正在启动 $JAR_NAME (Profile: $PROFILES)... \033[0m"
    cd "${WORKSPACE_HOME}/target" || exit 1
    
    BUILD_ID=dontKillMe nohup java $JAVA_OPT -jar "${TARGET_JAR}" > /dev/null 2>&1 &
    
    # 等待并检测是否启动成功
    sleep 3
    new_pid=$(get_pid)
    if [ -n "$new_pid" ]; then
        echo -e "\033[0;32m[成功] $JAR_NAME 启动成功，PID: $new_pid\033[0m"
    else
        echo -e "\033[0;31m[失败] $JAR_NAME 启动异常，请检查日志目录: ${WORKSPACE_HOME}/logs/\033[0m"
        return 1
    fi
}

function stop() {
    pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo -e "\033[0;33m[提示] $JAR_NAME 未运行，无需停止。\033[0m"
        return 0
    fi

    echo -e "\033[0;33m[停止] 正在停止 $JAR_NAME (PID: $pid)...\033[0m"
    kill $pid

    # 优雅停机等待，最长等待 30 秒
    for i in $(seq 1 30); do
        if ! ps -p $pid > /dev/null 2>&1; then
            echo -e "\033[0;32m[成功] $JAR_NAME 已优雅停机 (耗时: ${i}s)。\033[0m"
            return 0
        fi
        sleep 1
    done

    # 超时仍未退出则执行强制停止
    if ps -p $pid > /dev/null 2>&1; then
        echo -e "\033[0;31m[警告] 进程超时未响应，正在执行强制终止 (kill -9 $pid)...\033[0m"
        kill -9 $pid
        sleep 1
        echo -e "\033[0;32m[成功] $JAR_NAME 已强制终止。\033[0m"
    fi
}

function restart() {
    stop
    sleep 2
    start
}

function status() {
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "\033[0;32m[运行中] $JAR_NAME 正常运行中，PID: $pid\033[0m"
    else
        echo -e "\033[0;31m[未运行] $JAR_NAME 处于停止状态。\033[0m"
    fi
}

# ------------------------------------------------------------------------------
# 执行动作分发
# ------------------------------------------------------------------------------
case ${ACTION} in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        printUsage
        exit 1
        ;;
esac
