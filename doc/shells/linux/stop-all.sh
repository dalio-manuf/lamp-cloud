#!/bin/bash

# ==============================================================================
# lamp-cloud 微服务全量停止脚本（逆序停止）
# ==============================================================================

echo -e "\033[0;33m========== 开始批量停止微服务 ==========\033[0m"

SERVICES=(
    "lamp-base-server"
    "lamp-system-server"
    "lamp-oauth-server"
    "lamp-gateway-server"
)

for SERVICE in "${SERVICES[@]}"; do
    echo -e "\n\033[0;34m--> 正在停止 $SERVICE ...\033[0m"
    sh run.sh stop "$SERVICE"
done

echo -e "\n\033[0;32m========== 全部微服务已停止 ==========\033[0m"
