#!/bin/bash

# ==============================================================================
# Nacos 2.x 单机启动脚本
# ==============================================================================

MYSQL_HOST=${MYSQL_HOST:-127.0.0.1}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DB=${MYSQL_DB:-lamp_nacos}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PWD=${MYSQL_PWD:-root}

docker stop lamp_nacos 2>/dev/null
docker rm lamp_nacos 2>/dev/null

echo "Starting lamp_nacos (Nacos 2.x)..."

docker run -idt --name lamp_nacos --restart=always \
    -e JVM_XMS=512m -e JVM_XMX=512m -e JVM_XMN=256m \
    -e PREFER_HOST_MODE=hostname \
    -e MODE=standalone \
    -e SPRING_DATASOURCE_PLATFORM=mysql \
    -e MYSQL_DATABASE_NUM=1 \
    -e MYSQL_SERVICE_HOST=$MYSQL_HOST \
    -e MYSQL_SERVICE_PORT=$MYSQL_PORT \
    -e MYSQL_SERVICE_DB_NAME=$MYSQL_DB \
    -e MYSQL_SERVICE_USER=$MYSQL_USER \
    -e MYSQL_SERVICE_PASSWORD=$MYSQL_PWD \
    -e NACOS_AUTH_ENABLE=false \
    -p 8848:8848 \
    -p 9848:9848 \
    -p 9849:9849 \
    -v $(pwd)/logs/:/home/nacos/logs \
    -v $(pwd)/init.d/custom.properties:/home/nacos/init.d/custom.properties \
    nacos/nacos-server:v2.3.2
