# lamp-cloud 运维与工程文档中心

欢迎查阅 `lamp-cloud` 平台的文档与运维资源目录。本文件夹汇集了系统底层初始化数据、多数据库脚本、容器化编排文件、启停脚本及关键技术原理手册。

---

## 目录结构速览

```text
doc/
├── README.md                          # 文档中心总览与索引导航（本文档）
├── sql/                               # 多数据库方言初始化脚本
│   ├── mysql/                         # MySQL 8.0 / 5.7 建库与业务表结构
│   ├── dameng/                        # 达梦数据库 (DM8) 初始化脚本
│   ├── oracle/                        # Oracle 12c+ 用户创建与授权脚本
│   ├── sqlserver/                     # SQL Server 初始化脚本
│   └── 注意.md                        # 数据库初始化操作规范与注意事项
├── docker/                            # Docker 容器化运维专题指南
│   ├── 01.环境初始化(可选).md          # Linux 宿主机基础配置调优
│   ├── 02.docker安装.md               # 现代化 Docker CE 与 Compose 官方安装指引
│   └── 03.docker运行项目.md           # 微服务打包、镜像构建与容器运行全流程
├── dockerfile/                        # 中间件容器化配置文件与编排
│   ├── docker-compose.yml             # 基础中间件一键编排 (MySQL, Redis, Nacos, RabbitMQ, MinIO)
│   ├── .env.example                   # 环境变量配置模板
│   ├── nacos/                         # Nacos 2.x 容器配置与启动脚本
│   ├── redis/                         # Redis 容器配置与启动脚本
│   ├── mysql/ & mysql8/               # MySQL 5.7 / 8.0 容器配置与启动脚本
│   ├── rabbitmq/                      # RabbitMQ 容器启动脚本
│   ├── MinIo/                         # MinIO 对象存储容器启动脚本
│   ├── nginx/                         # 前端代理 Nginx 配置与反向代理模板
│   └── xxFileView/                    # kkFileView 在线文件预览配置
├── shells/                            # 本地与服务器启停运维脚本
│   ├── linux/                         # Linux 环境 Shell 启停脚本 (run.sh, start-all.sh 等)
│   └── window/                        # Windows 本地快速调试批处理脚本 (*.bat)
├── third-party/                       # 第三方中间件预置数据
│   └── nacos/                         # Nacos 控制台微服务初始配置导出包 (.zip)
├── image/                             # 系统架构图、链路监控截图与业务全景图
├── FastDFS安装文档_centos7.md          # FastDFS 分布式文件存储安装配置方案
├── hystrix配置详解.md                  # 微服务超时计算、隔离策略与容错降级手册
├── ip2region_v4.xdb                   # IPv4 离线快速定位解析数据库
└── ip2region_v6.xdb                   # IPv6 离线快速定位解析数据库
```

---

## 快速上手推荐路径

对于初次搭建或部署本项目的开发者，推荐遵循以下标准实施路径：

```text
[Step 1: 基础中间件拉起] 
    使用 doc/dockerfile/docker-compose.yml 快速启动 MySQL、Redis、Nacos 等中间件
        ↓
[Step 2: 数据库导入与配置] 
    执行 doc/sql/ 对应数据库脚本，并将 doc/third-party/nacos/ 导出包导入 Nacos
        ↓
[Step 3: 后端工程打包编译] 
    项目根目录下执行 mvn clean package -DskipTests
        ↓
[Step 4: 服务启动与运维] 
    方式 A (裸机部署)：使用 doc/shells/ 对应的 Linux Shell 或 Windows Bat 脚本启动
    方式 B (容器部署)：参考 doc/docker/03.docker运行项目.md 构建镜像并拉起容器
```

---

## 模块文档索引

| 专题模块 | 入口文档 / 资源 | 核心用途与指引 |
| :--- | :--- | :--- |
| **数据库初始化** | [doc/sql/注意.md](sql/注意.md) | MySQL、达梦、Oracle、SQL Server 建库与表结构导入规范 |
| **Docker 部署** | [doc/docker/03.docker运行项目.md](docker/03.docker运行项目.md) | 涵盖环境初始化、Docker 安装与微服务镜像编排运行 |
| **中间件编排** | [doc/dockerfile/docker-compose.yml](dockerfile/docker-compose.yml) | 本地及测试环境一键拉起全套基础服务依赖 |
| **服务运维脚本** | [doc/shells/linux/run.sh](shells/linux/run.sh) | 支持优雅停机、状态检测、一键全量启停的运维脚本体系 |
| **高并发容错** | [doc/hystrix配置详解.md](hystrix配置详解.md) | 超时联动计算公式、线程隔离与熔断器参数调优手册 |
| **文件存储方案** | [doc/FastDFS安装文档_centos7.md](FastDFS安装文档_centos7.md) | 传统 FastDFS 源码编译与推荐的容器化极简部署 |
