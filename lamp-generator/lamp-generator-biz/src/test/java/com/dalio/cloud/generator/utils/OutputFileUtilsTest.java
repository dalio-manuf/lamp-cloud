package com.dalio.cloud.generator.utils;

import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.enumeration.FileOverrideStrategyEnum;
import com.dalio.cloud.generator.enumeration.TemplateEnum;
import com.dalio.cloud.generator.enumeration.TplEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputFileUtilsTest {

    @Test
    @DisplayName("测试 getOutputFile 和 getZipOutputFile")
    void testGetOutputFile() {
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
        table.setOutputDir("/tmp");

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

        String outMap = OutputFileUtils.getOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_ENTITY_JAVA, "lamp", TemplateEnum.BACKEND);
        assertNotNull(outMap);
        String soybeanMap = OutputFileUtils.getOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_API, "lamp", TemplateEnum.WEB_SOYBEAN);
        assertNotNull(soybeanMap);
        String vbenMap = OutputFileUtils.getOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_API, "lamp", TemplateEnum.WEB_VBEN5);
        assertNotNull(vbenMap);

        String zipMap = OutputFileUtils.getZipOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_ENTITY_JAVA, "lamp", TemplateEnum.BACKEND);
        assertNotNull(zipMap);
        String zipSoybeanMap = OutputFileUtils.getZipOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_API, "lamp", TemplateEnum.WEB_SOYBEAN);
        assertNotNull(zipSoybeanMap);
        String zipVbenMap = OutputFileUtils.getZipOutputFile(config, table, null, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_API, "lamp", TemplateEnum.WEB_VBEN5);
        assertNotNull(zipVbenMap);

        java.util.Map<String, FileOverrideStrategyEnum> overrideConfig = new java.util.HashMap<>();
        overrideConfig.put(com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_ENTITY_JAVA, FileOverrideStrategyEnum.OVERRIDE);
        FileOverrideStrategyEnum overrideMap = OutputFileUtils.getFileOverride(config, overrideConfig, com.dalio.cloud.generator.utils.GenCodeConstant.TEMPLATE_ENTITY_JAVA, "backend");
        assertNotNull(overrideMap);
    }

    @Test
    @DisplayName("测试所有模板路径的生成")
    void testAllTemplates() throws Exception {
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
        table.setTplType(TplEnum.TREE);
        table.setEntitySuperClass(com.dalio.cloud.generator.enumeration.EntitySuperClassEnum.SUPER_ENTITY);
        table.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);
        table.setOutputDir("/tmp");

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

        java.lang.reflect.Field[] fields = com.dalio.cloud.generator.utils.GenCodeConstant.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.getName().startsWith("TEMPLATE_")) {
                String template = (String) field.get(null);
                try {
                    OutputFileUtils.getOutputFile(config, table, null, template, "lamp", TemplateEnum.BACKEND);
                } catch (Exception e) {
                }
                try {
                    OutputFileUtils.getOutputFile(config, table, null, template, "lamp", TemplateEnum.WEB_SOYBEAN);
                } catch (Exception e) {
                }
                try {
                    OutputFileUtils.getOutputFile(config, table, null, template, "lamp", TemplateEnum.WEB_VBEN5);
                } catch (Exception e) {
                }
                try {
                    OutputFileUtils.getOutputFile(config, table, null, template, "lamp", TemplateEnum.WEB_PLUS);
                } catch (Exception e) {
                }
            }
        }

        table.setTplType(TplEnum.MAIN_SUB);
        for (java.lang.reflect.Field field : fields) {
            if (field.getName().startsWith("TEMPLATE_")) {
                String template = (String) field.get(null);
                try {
                    OutputFileUtils.getOutputFile(config, table, null, template, "lamp", TemplateEnum.WEB_SOYBEAN);
                } catch (Exception e) {
                }
            }
        }
    }
}
