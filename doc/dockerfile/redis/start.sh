#!/bin/bash

# ==============================================================================
# Redis 容器启动脚本
# ==============================================================================

REDIS_PORT=${REDIS_PORT:-6379}
DATA_DIR=${DATA_DIR:-/data/docker-data/redis-data}

mkdir -p "$DATA_DIR"

docker stop lamp_redis 2>/dev/null
docker rm lamp_redis 2>/dev/null

echo "Starting lamp_redis on port $REDIS_PORT..."

docker run -idt -p $REDIS_PORT:6379 --name lamp_redis --restart=always \
    -v $(pwd)/redis.conf:/etc/redis/redis_default.conf \
    -v "$DATA_DIR":/data \
    -e TZ="Asia/Shanghai" \
    redis:7.2-alpine redis-server /etc/redis/redis_default.conf --appendonly yes
