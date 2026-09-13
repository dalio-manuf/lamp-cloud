# FastDFS 分布式文件系统安装与部署指南

本文档介绍 FastDFS 分布式文件系统的部署与配置。提供 **Docker 快速部署（推荐）** 与 **CentOS 7 源码编译部署** 两种方案。

> [!TIP]
> **部署建议**：CentOS 7 已于 2024 年 6 月正式停止维护（EOL），生产与测试环境推荐优先采用 **方案一：Docker 容器化部署**，开箱即用且易于维护迁移。

---

## 方案一：Docker 容器化快速部署（推荐）

使用 Docker 可以在几分钟内快速拉起一套包含 Tracker 与 Storage 的 FastDFS 实例。

### 1. 拉取 FastDFS 镜像

```bash
docker pull delron/fastdfs
```

### 2. 启动 Tracker 追踪服务器

```bash
docker run -d --name tracker --net=host \
  -v /data/fastdfs/tracker:/var/fdfs \
  delron/fastdfs tracker
```

- 默认监听端口：`22122`
- 本机数据持久化目录：`/data/fastdfs/tracker`

### 3. 启动 Storage 存储服务器

将 `<YOUR_SERVER_IP>` 替换为宿主机的实际局域网或公网 IP（不可使用 `127.0.0.1`）：

```bash
docker run -d --name storage --net=host \
  -e TRACKER_SERVER=<YOUR_SERVER_IP>:22122 \
  -v /data/fastdfs/storage:/var/fdfs \
  delron/fastdfs storage
```

- 默认 Storage 通信端口：`23000`
- 默认 Nginx HTTP 访问端口：`8888`

### 4. 验证上传

进入 Storage 容器内部测试文件上传：

```bash
docker exec -it storage /bin/bash
# 在容器内生成测试文件并上传
echo "Hello FastDFS" > test.txt
/usr/bin/fdfs_upload_file /etc/fdfs/client.conf test.txt
```

若成功将返回文件路径，例如：`group1/M00/00/00/xxx.txt`。通过浏览器访问 `http://<YOUR_SERVER_IP>:8888/group1/M00/00/00/xxx.txt` 即可查看文件。

---

## 方案二：CentOS 7 源码编译部署

### 1. 部署架构规划

| 角色 | 推荐端口 | 说明 |
| :--- | :--- | :--- |
| **Tracker Server** | 22122 | 调度中心，负责负载均衡与调度 |
| **Storage Server** | 23000 | 存储节点，负责文件存储与同步 |
| **Nginx Web 服务** | 8888 / 6080 | 整合 `fastdfs-nginx-module` 提供 HTTP 外部访问 |

> 占位符说明：下文中 `${TRACKER_SERVER_IP}` 代表 Tracker 所在机器 IP，`${STORAGE_SERVER_IP}` 代表 Storage 所在机器 IP。

### 2. 编译基础依赖包

```bash
yum install -y gcc gcc-c++ autoconf automake make zlib zlib-devel pcre pcre-devel openssl openssl-devel libevent
```

### 3. 安装 libfastcommon

```bash
cd /usr/local/src/
# 解压 libfastcommon
unzip libfastcommon-master.zip
cd libfastcommon-master
./make.sh
sudo ./make.sh install

# 创建软链接避免找不到动态库
sudo ln -s /usr/lib64/libfastcommon.so /usr/local/lib/libfastcommon.so
sudo ln -s /usr/lib64/libfastcommon.so /usr/lib/libfastcommon.so
```

### 4. 安装 FastDFS 服务端

```bash
cd /usr/local/src/
tar -zxvf FastDFS_v5.05.tar.gz
cd FastDFS
./make.sh
sudo ./make.sh install

# 复制默认配置文件模板
cd /etc/fdfs/
sudo cp client.conf.sample client.conf
sudo cp storage.conf.sample storage.conf
sudo cp tracker.conf.sample tracker.conf
```

### 5. 配置与启动 Tracker

编辑 `/etc/fdfs/tracker.conf`：

```ini
# 启用配置
disabled=false
# Tracker 端口
port=22122
# 基础工作目录（需提前 mkdir -p /home/admin/fastdfs/tracker）
base_path=/home/admin/fastdfs/tracker
# HTTP 端口（若有）
http.server_port=6080
```

启动 Tracker 服务：
```bash
/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf start
# 查看日志确认启动状态
tail -100f /home/admin/fastdfs/tracker/logs/trackerd.log
```

### 6. 配置与启动 Storage

编辑 `/etc/fdfs/storage.conf`：

```ini
disabled=false
group_name=group1
port=23000
# 基础日志目录
base_path=/home/admin/fastdfs/storage
# 实际文件存储路径数量
store_path_count=1
store_path0=/home/admin/fastdfs/storage
# Tracker 调度服务器地址（切勿填 127.0.0.1）
tracker_server=${TRACKER_SERVER_IP}:22122
http.server_port=7888
```

启动 Storage 服务：
```bash
/usr/bin/fdfs_storaged /etc/fdfs/storage.conf start
# 查看日志确认与 Tracker 连接正常
tail -100f /home/admin/fastdfs/storage/logs/storaged.log
```

### 7. 配置 Nginx 模块支持 HTTP 访问

FastDFS 自 5.05+ 版本起已移除了内置 HTTP 支持，需配合 Nginx 及 `fastdfs-nginx-module` 扩展：

1. **编译安装 Nginx**：
   ```bash
   cd /usr/local/src/nginx-1.16.1
   ./configure --prefix=/usr/local/nginx \
       --add-module=/usr/local/src/fastdfs-nginx-module/src
   make && sudo make install
   ```

2. **配置 mod_fastdfs.conf**：
   ```bash
   cp /usr/local/src/fastdfs-nginx-module/src/mod_fastdfs.conf /etc/fdfs/
   cp /usr/local/src/FastDFS/conf/http.conf /etc/fdfs/
   cp /usr/local/src/FastDFS/conf/mime.types /etc/fdfs/
   ```

   编辑 `/etc/fdfs/mod_fastdfs.conf`：
   ```ini
   base_path=/home/admin/fastdfs/storage
   tracker_server=${TRACKER_SERVER_IP}:22122
   storage_server_port=23000
   url_have_group_name=true
   store_path0=/home/admin/fastdfs/storage
   group_count=1
   ```

3. **配置 Nginx 虚拟主机**：
   编辑 `/usr/local/nginx/conf/nginx.conf`：
   ```nginx
   server {
       listen       6080;
       server_name  localhost;

       location ~/group[0-9]/M00 {
           root    /home/admin/fastdfs/storage/data;
           ngx_fastdfs_module;
       }

       error_page   500 502 503 504  /50x.html;
       location = /50x.html {
           root   html;
       }
   }
   ```

4. **启动 Nginx**：
   ```bash
   /usr/local/nginx/sbin/nginx
   ```

---

## 常用运维命令速查

| 操作 | 对应命令 |
| :--- | :--- |
| **启动 Tracker** | `/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf start` |
| **重启 Tracker** | `/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf restart` |
| **停止 Tracker** | `/usr/bin/fdfs_trackerd /etc/fdfs/tracker.conf stop` |
| **启动 Storage** | `/usr/bin/fdfs_storaged /etc/fdfs/storage.conf start` |
| **重启 Storage** | `/usr/bin/fdfs_storaged /etc/fdfs/storage.conf restart` |
| **停止 Storage** | `/usr/bin/fdfs_storaged /etc/fdfs/storage.conf stop` |
| **检查节点状态** | `/usr/bin/fdfs_monitor /etc/fdfs/storage.conf` |
| **命令行上传测试** | `/usr/bin/fdfs_upload_file /etc/fdfs/client.conf <待上传文件>` |
