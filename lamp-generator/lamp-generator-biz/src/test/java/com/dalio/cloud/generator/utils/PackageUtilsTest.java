package com.dalio.cloud.generator.utils;

import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.enumeration.TemplateEnum;
import com.dalio.cloud.generator.enumeration.TplEnum;
import com.dalio.cloud.generator.utils.inner.PackageUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageUtilsTest {

    @Test
    @DisplayName("测试 getPackage")
    void testGetPackage() {
        DefGenTable table = new DefGenTable();
        table.setId(10L);
        table.setName("test_user");
        table.setEntityName("TestUser");
        table.setServiceName("test");
        table.setModuleName("user");
        table.setPlusApplicationName("testAdmin");
        table.setPlusModuleName("user");
        table.setMenuApplicationId(100L);
        table.setAuthor("admin");
        table.setParent("com.dalio.cloud");
        table.setTplType(TplEnum.SIMPLE);
        table.setEntitySuperClass(com.dalio.cloud.generator.enumeration.EntitySuperClassEnum.SUPER_ENTITY);
        table.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);

        GeneratorConfig config = new GeneratorConfig();
        config.setProjectPrefix("lamp");
        config.setAuthor("admin");
        config.setOutputDir("/tmp");
        config.setPackageInfoConfig(new com.dalio.cloud.generator.config.PackageInfoConfig());
        config.setEntityConfig(new com.dalio.cloud.generator.config.EntityConfig());
        config.setMapperConfig(new com.dalio.cloud.generator.config.MapperConfig());
        config.setServiceConfig(new com.dalio.cloud.generator.config.ServiceConfig());
        config.setManagerConfig(new com.dalio.cloud.generator.config.ManagerConfig());
        config.setControllerConfig(new com.dalio.cloud.generator.config.ControllerConfig());
        config.setFileOverrideStrategy(new com.dalio.cloud.generator.config.FileOverrideStrategy());

        Map<String, Object> packageMap = PackageUtils.getPackage(table, config);
        assertNotNull(packageMap);
        
        Map<String, Object> superPackageMap = PackageUtils.getSuperClassPackage(table);
        assertNotNull(superPackageMap);

        java.util.List<com.dalio.cloud.generator.entity.DefGenTableColumn> columns = new java.util.ArrayList<>();
        
        com.dalio.cloud.generator.entity.DefGenTableColumn col = new com.dalio.cloud.generator.entity.DefGenTableColumn();
        col.setJavaType("Long");
        col.setJavaField("id");
        col.setIsPk(true);
        columns.add(col);

        com.dalio.cloud.generator.entity.DefGenTableColumn col2 = new com.dalio.cloud.generator.entity.DefGenTableColumn();
        col2.setJavaType("BigDecimal");
        col2.setJavaField("price");
        col2.setIsPk(false);
        columns.add(col2);

        com.dalio.cloud.generator.entity.DefGenTableColumn col3 = new com.dalio.cloud.generator.entity.DefGenTableColumn();
        col3.setJavaType("LocalDateTime");
        col3.setJavaField("createTime");
        col3.setFill("INSERT");
        columns.add(col3);

        
        Map<String, Object> objectMap = new java.util.HashMap<>();
        objectMap.put("superEntityClass", "SuperEntity");

        Map<String, Object> importPackages = PackageUtils.getImportPackages(com.baomidou.mybatisplus.annotation.DbType.MYSQL, table, config, columns, objectMap);
        assertNotNull(importPackages);

        Map<String, Object> mvcPackage = PackageUtils.getMvcPackage(table, config, new java.util.HashMap<>());
        assertNotNull(mvcPackage);

        String name = PackageUtils.getName("Test", "%sImpl", "Service");
        assertNotNull(name);
    }
}
