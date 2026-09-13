#!/bin/bash
# 注意: authority 模块在当前架构下已整合至 system 服务中
PROFILE=${1:-prod}
sh run.sh restart lamp-system-server "$PROFILE"
