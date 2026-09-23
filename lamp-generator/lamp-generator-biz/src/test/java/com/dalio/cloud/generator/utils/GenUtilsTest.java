package com.dalio.cloud.generator.utils;

import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import com.baomidou.mybatisplus.annotation.DbType;
import com.dalio.cloud.generator.config.EntityConfig;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.config.MapperConfig;
import com.dalio.cloud.generator.config.ServiceConfig;
import com.dalio.cloud.generator.config.WebProConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenUtilsTest {

    @Test
    @DisplayName("测试 initTable")
    void testInitTable() {
        GeneratorConfig config = new GeneratorConfig();
        config.setAuthor("admin");
        config.setOutputDir("/tmp");

        ServiceConfig serviceConfig = new ServiceConfig();
        java.util.Set<String> dsPrefix = new java.util.HashSet<>();
        dsPrefix.add("ds_");
        serviceConfig.setDsTablePrefix(dsPrefix);
        config.setServiceConfig(serviceConfig);

        MapperConfig mapperConfig = new MapperConfig();
        java.util.Set<String> tenantPrefix = new java.util.HashSet<>();
        tenantPrefix.add("tenant_");
        mapperConfig.setColumnAnnotationTablePrefix(tenantPrefix);
        config.setMapperConfig(mapperConfig);

        EntityConfig entityConfig = new EntityConfig();
        entityConfig.setLombok(true);
        config.setEntityConfig(entityConfig);

        WebProConfig webProConfig = new WebProConfig();
        config.setWebProConfig(webProConfig);

        com.dalio.cloud.generator.config.PackageInfoConfig packageInfoConfig = new com.dalio.cloud.generator.config.PackageInfoConfig();
        packageInfoConfig.setParent("com.dalio.cloud");
        config.setPackageInfoConfig(packageInfoConfig);

        Table tableMeta = Table.create("ds_tenant_user");
        tableMeta.setComment("用户表");

        DefGenTable genTable = GenUtils.initTable(config, tableMeta);
        assertNotNull(genTable);
        assertEquals("ds_tenant_user", genTable.getName());
        assertEquals("用户表", genTable.getComment());
        assertTrue(genTable.getIsDs());
    }

    @Test
    @DisplayName("测试 convertClassName")
    void testConvertClassName() {
        GeneratorConfig config = new GeneratorConfig();
        List<String> prefix = new ArrayList<>();
        prefix.add("test_");
        config.setTablePrefix(prefix);

        String className = GenUtils.convertClassName(config, "test_user_info");
        assertEquals("UserInfo", className);

        String className2 = GenUtils.convertClassName(config, "user_info");
        assertEquals("UserInfo", className2);

        config.setTablePrefix(new ArrayList<>());
        String className3 = GenUtils.convertClassName(config, "test_user_info");
        assertEquals("TestUserInfo", className3);
    }

    @Test
    @DisplayName("测试 initColumnField")
    void testInitColumnField() {
        GeneratorConfig config = new GeneratorConfig();
        EntityConfig entityConfig = new EntityConfig();
        List<String> ignoreColumns = new ArrayList<>();
        ignoreColumns.add("ignore_me");
        entityConfig.setIgnoreColumns(ignoreColumns);
        entityConfig.setDateType(com.dalio.cloud.generator.config.DateType.TIME_PACK);
        config.setEntityConfig(entityConfig);

        DefGenTable genTable = new DefGenTable();
        genTable.setName("test");
        genTable.setEntityName("Test");

        Column col1 = new Column();
        col1.setTableName("test");
        col1.setName("ignore_me");
        DefGenTableColumn res1 = GenUtils.initColumnField(config, DbType.MYSQL, genTable, col1);
        assertNull(res1);

        Column col2 = new Column();
        col2.setTableName("test");
        col2.setName("user_id");
        col2.setType(java.sql.Types.BIGINT);
        col2.setTypeName("BIGINT");
        col2.setComment("用户ID");
        DefGenTableColumn res2 = GenUtils.initColumnField(config, DbType.MYSQL, genTable, col2);
        assertNotNull(res2);
        assertEquals("userId", res2.getJavaField());
        assertEquals("Long", res2.getJavaType());

        Column col3 = new Column();
        col3.setTableName("test");
        col3.setName("status");
        col3.setType(java.sql.Types.VARCHAR);
        col3.setTypeName("VARCHAR");
        col3.setComment("状态 @Echo(api = \"dict\", dictType = \"status\")");
        DefGenTableColumn res3 = GenUtils.initColumnField(config, DbType.MYSQL, genTable, col3);
        assertNotNull(res3);
        assertEquals("@Echo(api = \"dict\", dictType = \"status\")", res3.getEchoStr());
    }
}
