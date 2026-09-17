package com.dalio.cloud.generator.utils;

import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.generator.config.EntityConfig;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.config.MapperConfig;
import com.dalio.cloud.generator.config.PackageInfoConfig;
import com.dalio.cloud.generator.config.ServiceConfig;
import com.dalio.cloud.generator.config.WebProConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.enumeration.EntitySuperClassEnum;
import com.dalio.cloud.generator.enumeration.SuperClassEnum;
import com.dalio.cloud.generator.rules.DbColumnType;
import com.dalio.cloud.generator.utils.inner.PackageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PackageAndGenUtilsTest {

    @Test
    @DisplayName("测试 PackageUtils.getPackage 与 getSuperClassPackage")
    void testPackageUtilsGetPackage() {
        GeneratorConfig config = new GeneratorConfig();
        PackageInfoConfig pkgConfig = new PackageInfoConfig();
        pkgConfig.setParent("com.dalio.cloud");
        config.setPackageInfoConfig(pkgConfig);

        DefGenTable table = new DefGenTable();
        table.setParent("com.dalio.cloud");
        table.setModuleName("system");
        table.setChildPackageName("tenant");
        table.setSuperClass(SuperClassEnum.SUPER_CLASS);
        table.setEntitySuperClass(EntitySuperClassEnum.SUPER_ENTITY);

        Map<String, Object> pkgMap = PackageUtils.getPackage(table, config);
        assertNotNull(pkgMap);
        assertTrue(pkgMap.containsKey(GenCodeConstant.ENTITY));
        assertTrue(pkgMap.containsKey(GenCodeConstant.MAPPER));
        assertTrue(pkgMap.containsKey(GenCodeConstant.SERVICE));

        Map<String, Object> superPkgMap = PackageUtils.getSuperClassPackage(table);
        assertNotNull(superPkgMap);
    }

    @Test
    @DisplayName("测试 PackageUtils.getName 格式化")
    void testPackageUtilsGetName() {
        assertEquals("UserService", PackageUtils.getName("User", "{}Service", "Service"));
        assertEquals("UserMapper", PackageUtils.getName("User", null, "Mapper"));
        assertEquals("User", PackageUtils.getName("User", null, ""));
    }

    @Test
    @DisplayName("测试 PackageUtils.getImportPackages 生成各类 import 依赖")
    void testPackageUtilsInitPackage() {
        GeneratorConfig config = new GeneratorConfig();
        config.setPackageInfoConfig(new PackageInfoConfig());
        config.setEntityConfig(new EntityConfig());
        config.setMapperConfig(new MapperConfig());
        config.setServiceConfig(new ServiceConfig());
        config.setControllerConfig(new com.dalio.cloud.generator.config.ControllerConfig());
        config.setManagerConfig(new com.dalio.cloud.generator.config.ManagerConfig());

        DefGenTable table = new DefGenTable();
        table.setEntityName("TestUser");
        table.setModuleName("test");
        table.setParent("com.dalio.cloud");
        table.setSuperClass(SuperClassEnum.SUPER_CLASS);
        table.setEntitySuperClass(EntitySuperClassEnum.ENTITY);

        DefGenTableColumn col1 = new DefGenTableColumn();
        col1.setName("created_at");
        col1.setJavaField("createdAt");
        col1.setJavaType(DbColumnType.LOCAL_DATE_TIME.getType());

        DefGenTableColumn col2 = new DefGenTableColumn();
        col2.setName("is_deleted");
        col2.setJavaField("isDeleted");
        col2.setIsLogicDeleteField(true);
        col2.setJavaType(DbColumnType.BOOLEAN.getType());

        List<DefGenTableColumn> columns = List.of(col1, col2);

        Map<String, Object> objectMap = new java.util.HashMap<>();
        Map<String, Object> importMap = PackageUtils.getImportPackages(DbType.MYSQL, table, config, columns, objectMap);
        assertNotNull(importMap);
        assertTrue(importMap.containsKey("entityImport"));
        assertTrue(importMap.containsKey("mapperImport"));
        assertTrue(importMap.containsKey("serviceImport"));
        assertTrue(importMap.containsKey("controllerImport"));
    }

    @Test
    @DisplayName("测试 GenUtils.initTable 与字段推断")
    void testGenUtilsInitTable() {
        GeneratorConfig config = new GeneratorConfig();
        config.setAuthor("Admin");
        config.setTablePrefix(List.of("c_"));
        config.setPackageInfoConfig(new PackageInfoConfig());
        config.setEntityConfig(new EntityConfig());
        config.setMapperConfig(new MapperConfig());
        config.setServiceConfig(new ServiceConfig());
        config.setWebProConfig(new WebProConfig());

        Table tableMeta = Table.create("c_user");
        tableMeta.setComment("用户表;系统核心用户信息");

        DefGenTable genTable = GenUtils.initTable(config, tableMeta);
        assertNotNull(genTable);
        assertEquals("c_user", genTable.getName());
        assertEquals("用户表", genTable.getSwaggerComment());
        assertEquals("User", genTable.getEntityName());
        assertEquals("Admin", genTable.getAuthor());
    }

    @Test
    @DisplayName("测试 GenUtils.initColumnField 字段类型与填充规则映射")
    void testGenUtilsInitColumn() {
        GeneratorConfig config = new GeneratorConfig();
        EntityConfig entityConfig = new EntityConfig();
        entityConfig.setFillPropertyName(new java.util.HashMap<>(Map.of("createdTime", FieldFill.INSERT)));
        config.setEntityConfig(entityConfig);
        config.setWebProConfig(new WebProConfig());

        DefGenTable table = new DefGenTable();
        table.setEntitySuperClass(EntitySuperClassEnum.SUPER_ENTITY);

        Column colMeta = new Column();
        colMeta.setName("created_time");
        colMeta.setType(93); // TIMESTAMP
        colMeta.setComment("创建时间");

        DefGenTableColumn column = GenUtils.initColumnField(config, DbType.MYSQL, table, colMeta);
        assertNotNull(column);
        assertEquals("createdTime", column.getJavaField());
        assertEquals(FieldFill.INSERT.name(), column.getFill());
        assertNotNull(column.getJavaType());
    }
}
