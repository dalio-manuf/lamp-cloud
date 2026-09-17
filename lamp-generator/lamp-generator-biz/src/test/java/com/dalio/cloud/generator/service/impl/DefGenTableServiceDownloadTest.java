package com.dalio.cloud.generator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.manager.DefGenTableColumnManager;
import com.dalio.cloud.generator.manager.DefGenTableManager;
import com.dalio.cloud.generator.vo.save.DefGenVO;
import com.dalio.basic.database.properties.DatabaseProperties;
import com.baidu.fsg.uid.UidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefGenTableServiceDownloadTest {

    @Test
    @DisplayName("测试 downloadZip")
    void testDownloadZip() throws Exception {
        DefGenTableColumnManager columnManager = mock(DefGenTableColumnManager.class);
        DefGenTableManager tableManager = mock(DefGenTableManager.class);
        GeneratorConfig config = new GeneratorConfig();
        config.setOutputDir("/tmp/lamp-test-gen");
        config.setProjectPrefix("lamp");
        config.setPackageInfoConfig(new com.dalio.cloud.generator.config.PackageInfoConfig());
        config.setEntityConfig(new com.dalio.cloud.generator.config.EntityConfig());
        config.setMapperConfig(new com.dalio.cloud.generator.config.MapperConfig());
        config.setServiceConfig(new com.dalio.cloud.generator.config.ServiceConfig());
        config.setManagerConfig(new com.dalio.cloud.generator.config.ManagerConfig());
        config.setControllerConfig(new com.dalio.cloud.generator.config.ControllerConfig());
        config.setFileOverrideStrategy(new com.dalio.cloud.generator.config.FileOverrideStrategy());
        config.setConstantsPackage(new java.util.HashMap<>());

        com.dalio.basic.database.properties.DatabaseProperties dbProps = new com.dalio.basic.database.properties.DatabaseProperties();
        com.baidu.fsg.uid.UidGenerator uidGen = mock(com.baidu.fsg.uid.UidGenerator.class);

        DefGenTableServiceImpl service = new DefGenTableServiceImpl(columnManager, config, dbProps, uidGen);
        ReflectionTestUtils.setField(service, "superManager", tableManager);

        DefGenTable genTable = new DefGenTable();
        genTable.setId(10L);
        genTable.setName("test_table");
        genTable.setEntityName("TestTable");
        genTable.setServiceName("test");
        genTable.setModuleName("table");
        genTable.setPlusApplicationName("testAdmin");
        genTable.setPlusModuleName("table");
        genTable.setMenuApplicationId(100L);
        genTable.setAuthor("admin");
        genTable.setParent("com.dalio.cloud");
        genTable.setTplType(com.dalio.cloud.generator.enumeration.TplEnum.SIMPLE);
        genTable.setEntitySuperClass(com.dalio.cloud.generator.enumeration.EntitySuperClassEnum.SUPER_ENTITY);
        genTable.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);

        DefGenTableColumn col = new DefGenTableColumn();
        col.setId(1L);
        col.setTableId(10L);
        col.setName("id");
        col.setJavaType("Long");
        col.setJavaField("id");
        col.setIsPk(true);
        col.setTsType("string");
        col.setSize(20L);

        when(tableManager.getById(anyLong())).thenReturn(genTable);
        when(tableManager.getDbType()).thenReturn(com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        when(columnManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.singletonList(col));

        // Create mock response
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 模拟 Spring 工具类获取 RequestAttributes
        try (MockedStatic<org.springframework.web.context.request.RequestContextHolder> requestContextHolder = Mockito.mockStatic(org.springframework.web.context.request.RequestContextHolder.class)) {
            org.springframework.web.context.request.ServletRequestAttributes requestAttributes = mock(org.springframework.web.context.request.ServletRequestAttributes.class);
            when(requestAttributes.getResponse()).thenReturn(response);
            requestContextHolder.when(org.springframework.web.context.request.RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            service.downloadZip(Collections.singletonList(10L), com.dalio.cloud.generator.enumeration.TemplateEnum.BACKEND);

            assertNotNull(response.getContentAsByteArray());
        }
    }
    
    @Test
    @DisplayName("测试 previewCode")
    void testPreviewCode() {
        DefGenTableColumnManager columnManager = mock(DefGenTableColumnManager.class);
        DefGenTableManager tableManager = mock(DefGenTableManager.class);
        GeneratorConfig config = new GeneratorConfig();
        config.setOutputDir("/tmp/lamp-test-gen");
        config.setProjectPrefix("lamp");
        config.setPackageInfoConfig(new com.dalio.cloud.generator.config.PackageInfoConfig());
        config.setEntityConfig(new com.dalio.cloud.generator.config.EntityConfig());
        config.setMapperConfig(new com.dalio.cloud.generator.config.MapperConfig());
        config.setServiceConfig(new com.dalio.cloud.generator.config.ServiceConfig());
        config.setManagerConfig(new com.dalio.cloud.generator.config.ManagerConfig());
        config.setControllerConfig(new com.dalio.cloud.generator.config.ControllerConfig());
        config.setFileOverrideStrategy(new com.dalio.cloud.generator.config.FileOverrideStrategy());
        config.setConstantsPackage(new java.util.HashMap<>());

        com.dalio.basic.database.properties.DatabaseProperties dbProps = new com.dalio.basic.database.properties.DatabaseProperties();
        com.baidu.fsg.uid.UidGenerator uidGen = mock(com.baidu.fsg.uid.UidGenerator.class);

        DefGenTableServiceImpl service = new DefGenTableServiceImpl(columnManager, config, dbProps, uidGen);
        ReflectionTestUtils.setField(service, "superManager", tableManager);

        DefGenTable genTable = new DefGenTable();
        genTable.setId(10L);
        genTable.setName("test_table");
        genTable.setEntityName("TestTable");
        genTable.setServiceName("test");
        genTable.setModuleName("table");
        genTable.setPlusApplicationName("testAdmin");
        genTable.setPlusModuleName("table");
        genTable.setMenuApplicationId(100L);
        genTable.setAuthor("admin");
        genTable.setParent("com.dalio.cloud");
        genTable.setTplType(com.dalio.cloud.generator.enumeration.TplEnum.MAIN_SUB);
        genTable.setEntitySuperClass(com.dalio.cloud.generator.enumeration.EntitySuperClassEnum.SUPER_ENTITY);
        genTable.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);
        genTable.setSubId(20L);
        genTable.setSubJavaFieldName("subList");

        DefGenTable subTable = new DefGenTable();
        subTable.setId(20L);
        subTable.setName("sub_table");
        subTable.setEntityName("SubTable");
        subTable.setServiceName("sub");
        subTable.setModuleName("table");
        subTable.setPlusApplicationName("testAdmin");
        subTable.setPlusModuleName("sub");
        subTable.setMenuApplicationId(100L);
        subTable.setAuthor("admin");
        subTable.setParent("com.dalio.cloud");
        subTable.setTplType(com.dalio.cloud.generator.enumeration.TplEnum.SIMPLE);
        subTable.setEntitySuperClass(com.dalio.cloud.generator.enumeration.EntitySuperClassEnum.SUPER_ENTITY);
        subTable.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);

        DefGenTableColumn col = new DefGenTableColumn();
        col.setId(1L);
        col.setTableId(10L);
        col.setName("id");
        col.setJavaType("Long");
        col.setJavaField("id");
        col.setIsPk(true);
        col.setTsType("string");
        col.setSize(20L);

        when(tableManager.getById(10L)).thenReturn(genTable);
        when(tableManager.getById(20L)).thenReturn(subTable);
        when(tableManager.getDbType()).thenReturn(com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        when(columnManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.singletonList(col));

        Map<String, String> stringStringMap = service.previewCode(10L, com.dalio.cloud.generator.enumeration.TemplateEnum.BACKEND);
        assertNotNull(stringStringMap);
        
        genTable.setTplType(com.dalio.cloud.generator.enumeration.TplEnum.TREE);
        Map<String, String> stringStringMap2 = service.previewCode(10L, com.dalio.cloud.generator.enumeration.TemplateEnum.WEB_PLUS);
        assertNotNull(stringStringMap2);
    }
}
