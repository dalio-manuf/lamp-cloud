/*
 MySQL 数据库环境初始化说明：
 1. 业务脚本 (lamp_none.sql) 默认基于 MySQL 8.0 导出，使用的排序规则包含 utf8mb4_0900_ai_ci。
 2. 若使用 MySQL 5.7，请在导入 lamp_none.sql 前批量将 utf8mb4_0900_ai_ci 替换为 utf8mb4_general_ci。
 3. 推荐使用 MySQL 8.0+ 版本以获得更优的性能与完整的特性支持。
*/

-- 1. Nacos 配置中心与注册中心数据库
CREATE DATABASE IF NOT EXISTS `lamp_nacos` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. SkyWalking 链路追踪监控数据库 (可选)
CREATE DATABASE IF NOT EXISTS `lamp_sw` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3. lamp-none 核心业务数据库
CREATE DATABASE IF NOT EXISTS `lamp_none` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;



