/*
 Oracle 数据库环境初始化说明：
 1. Nacos 官方暂无内置 Oracle 支持，推荐方案：Nacos 配置中心与注册中心使用 MySQL，业务系统使用 Oracle。
 2. 若在 Oracle 12c/19c/21c 等容器多租户架构（CDB）下执行，需先切换至对应 PDB，或开启脚本标识：ALTER SESSION SET "_ORACLE_SCRIPT"=true;
 3. 请使用 SYSDBA 或具有管理员权限的用户登录并执行本脚本。
*/

-- 1. 创建核心业务用户/Schema
CREATE USER lamp_none IDENTIFIED BY "lamp_none";

-- 2. 分配默认表空间配额与系统权限
ALTER USER lamp_none DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE, CREATE SYNONYM TO lamp_none;

-- 3. (可选) SkyWalking 监控数据用户
-- CREATE USER lamp_sw IDENTIFIED BY "lamp_sw";
-- ALTER USER lamp_sw DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;
-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW, CREATE SEQUENCE TO lamp_sw;

