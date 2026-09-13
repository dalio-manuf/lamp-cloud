/*
 请注意，达梦数据库初始化要求：
 1. 务必设置数据库实例属性：“大小写敏感” = 否（CASE_SENSITIVE = 0）
 2. 字符集推荐使用 UTF-8
*/

-- nacos 数据库
CREATE SCHEMA lamp_nacos AUTHORIZATION "SYSDBA";
-- seata 数据库
CREATE SCHEMA lamp_seata AUTHORIZATION "SYSDBA";
-- SkyWalking 数据库
CREATE SCHEMA lamp_sw AUTHORIZATION "SYSDBA";

-- lamp-none 核心业务数据库
CREATE SCHEMA lamp_none AUTHORIZATION "SYSDBA";

