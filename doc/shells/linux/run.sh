#!/bin/bash

# ==============================================================================
# lamp-cloud 微服务运维管理脚本
# 支持操作: {start|stop|restart|status}
# 示例: sh run.sh start lamp-gateway-server prod
# ==============================================================================

ACTION=$1
MODULE=$2
PROFILES=$3

# 兼容旧参数名
if [ -z "$MODULE" ] && [ -n "$2" ]; then
    MODULE=$2
fi

if [ -z "$ACTION" ]; then
    echo -e "\033[0;31m[错误] 未输入操作名\033[0m \033[0;34m{start|stop|restart|status}\033[0m"
    exit 1
fi

if [ -z "$MODULE" ]; then
    echo -e "\033[0;31m[错误] 未输入服务应用名\033[0m (如: lamp-gateway-server, lamp-oauth-server)"
    exit 1
fi

# 去除可能传入的 .jar 后缀
MODULE_NAME=${MODULE%.jar}
JAR_NAME="${MODULE_NAME}.jar"

if [ -z "$PROFILES" ]; then
    PROFILES="prod"
fi

# JVM 参数配置
JAVA_OPT="-server -Xms1024m -Xmx2048m -Xss512k -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=512M -XX:+UseG1GC"
JAVA_OPT="$JAVA_OPT -Dspring.profiles.active=$PROFILES -Dfile.encoding=UTF-8"

# 获取进程 PID
get_pid() {
    echo $(ps -ef | grep -w "$JAR_NAME" | grep -v grep | awk '{print $2}')
}

start() {
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "\033[0;33m[提示] $MODULE_NAME 正在运行中，PID: $pid\033[0m"
        return 0
    fi

    if [ ! -f "$JAR_NAME" ]; then
        echo -e "\033[0;31m[错误] 未找到文件 $JAR_NAME，请确认当前目录下是否存在该 JAR 包！\033[0m"
        return 1
    fi

    echo -e "\033[0;32m[启动] 正在启动 $MODULE_NAME (Profile: $PROFILES)...\033[0m"
    nohup java $JAVA_OPT -jar "$JAR_NAME" > /dev/null 2>&1 &
    
    sleep 3
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "\033[0;32m[成功] $MODULE_NAME 启动成功，PID: $pid\033[0m"
    else
        echo -e "\033[0;31m[失败] $MODULE_NAME 启动失败，请检查相关日志\033[0m"
        return 1
    fi
}

stop() {
    pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo -e "\033[0;33m[提示] $MODULE_NAME 当前未在运行\033[0m"
        return 0
    fi

    echo -e "\033[0;34m[停止] 正在停止 $MODULE_NAME (PID: $pid)...\033[0m"
    kill "$pid" 2>/dev/null

    # 优雅停机等待（最多 15 秒）
    TIMEOUT=15
    while [ $TIMEOUT -gt 0 ]; do
        sleep 1
        pid=$(get_pid)
        if [ -z "$pid" ]; then
            echo -e "\033[0;32m[成功] $MODULE_NAME 已经安全停止\033[0m"
            return 0
        fi
        TIMEOUT=$((TIMEOUT - 1))
    done

    # 超时强制终止
    echo -e "\033[0;31m[警告] 超时未停止，执行强制终止 (kill -9)...\033[0m"
    kill -9 "$pid" 2>/dev/null
    sleep 1
    echo -e "\033[0;32m[成功] $MODULE_NAME 已强制停止\033[0m"
}

restart() {
    stop
    sleep 2
    start
}

status() {
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo -e "\033[0;32m[运行中] $MODULE_NAME 正在运行，PID: $pid\033[0m"
    else
        echo -e "\033[0;31m[已停止] $MODULE_NAME 未运行\033[0m"
    fi
}

case "$ACTION" in
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
        echo -e "\033[0;31m用法:\033[0m  \033[0;34msh $0 {start|stop|restart|status} {模块名称} [Profile环境]\033[0m"
        echo -e "\033[0;31m示例:\033[0m"
        echo -e "  \033[0;33msh $0 start lamp-gateway-server prod\033[0m"
        echo -e "  \033[0;33msh $0 restart lamp-oauth-server dev\033[0m"
        echo -e "  \033[0;33msh $0 stop lamp-system-server\033[0m"
        echo -e "  \033[0;33msh $0 status lamp-base-server\033[0m"
        exit 1
        ;;
esac
