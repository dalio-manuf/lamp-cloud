#!/bin/bash

# ==============================================================================
# lamp-cloud 微服务全量按序启动脚本
# ==============================================================================

PROFILE=${1:-prod}

echo -e "\033[0;32m========== 开始按依赖顺序批量启动微服务 (Profile: $PROFILE) ==========\033[0m"

SERVICES=(
    "lamp-gateway-server"
    "lamp-oauth-server"
    "lamp-system-server"
    "lamp-base-server"
)

for SERVICE in "${SERVICES[@]}"; do
    echo -e "\n\033[0;34m--> 正在启动 $SERVICE ...\033[0m"
    sh run.sh start "$SERVICE" "$PROFILE"
    sleep 3
done

echo -e "\n\033[0;32m========== 全部微服务启动完成 ==========\033[0m"
