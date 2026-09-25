package com.dalio.cloud.generator.utils;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.annotation.DbType;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.database.properties.DatabaseProperties;
import com.dalio.basic.database.properties.MultiTenantType;
import com.dalio.cloud.generator.config.ControllerConfig;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.enumeration.EntitySuperClassEnum;
import com.dalio.cloud.generator.rules.DbColumnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SourceCodeUtilsTest {

    @Test
    @DisplayName("测试 SourceCodeUtils.getObjectMap 组装模板参数")
    void testGetObjectMap() {
        GeneratorConfig config = new GeneratorConfig();
        config.setProjectPrefix("lamp");
        config.setAuthor("admin");
        ControllerConfig ctrlConfig = new ControllerConfig();
        ctrlConfig.setHyphenStyle(true);
        config.setControllerConfig(ctrlConfig);

        DatabaseProperties dbProps = new DatabaseProperties();
        dbProps.setMultiTenantType(MultiTenantType.COLUMN);
        dbProps.setTenantIdColumn("tenant_id");

        UidGenerator uidGenerator = Mockito.mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(1001L);

        DefGenTable genTable = new DefGenTable();
        genTable.setName("base_user");
        genTable.setEntityName("BaseUser");
        genTable.setParent("com.dalio.cloud");
        genTable.setModuleName("base");
        genTable.setAuthor("admin");
        genTable.setEntitySuperClass(EntitySuperClassEnum.SUPER_ENTITY);
        genTable.setSuperClass(com.dalio.cloud.generator.enumeration.SuperClassEnum.SUPER_CLASS);
        genTable.setMenuApplicationId(1L);
        genTable.setMenuParentId(0L);

        List<DefGenTableColumn> fields = new ArrayList<>();
        DefGenTableColumn pk = new DefGenTableColumn();
        pk.setName("id");
        pk.setJavaField("id");
        pk.setIsPk(true);
        pk.setJavaType("Long");
        fields.add(pk);

        DefGenTableColumn colCreated = new DefGenTableColumn();
        colCreated.setName("created_time");
        colCreated.setJavaField("createdTime");
        colCreated.setJavaType(DbColumnType.LOCAL_DATE_TIME.getType());
        fields.add(colCreated);

        DefGenTableColumn colDict = new DefGenTableColumn();
        colDict.setName("status");
        colDict.setJavaField("status");
        colDict.setJavaType("String");
        colDict.setComment("用户状态 [01-正常 02-禁用]");
        colDict.setEchoStr("@Echo(api = Echo.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.STATUS)");
        fields.add(colDict);

        ContextUtil.setUserId(888L);
        try {
            Map<String, Object> map = SourceCodeUtils.getObjectMap(config, dbProps, uidGenerator, null, genTable, fields, DbType.MYSQL);
            assertNotNull(map);
            assertEquals("admin", map.get("author"));
            assertEquals("com.dalio.cloud", map.get("parent"));
            assertEquals("mysql", map.get("dbType"));
            assertNotNull(map.get("pkField"));
            assertNotNull(map.get("allFields"));
            assertNotNull(map.get("dictList"));
            assertTrue(map.containsKey("menuId"));
            assertEquals(888L, map.get("createdBy"));

            // 测试 subObjectMap 非空传入
            Map<String, Object> subMap = Map.of("subKey", "subVal");
            Map<String, Object> mapWithSub = SourceCodeUtils.getObjectMap(config, dbProps, uidGenerator, subMap, genTable, fields, DbType.MYSQL);
            assertEquals(subMap, mapWithSub.get("sub"));
        } finally {
            ContextUtil.remove();
        }
    }

    @Test
    @DisplayName("测试 SourceCodeUtils 主键校验防御")
    void testPkValidation() {
        GeneratorConfig config = new GeneratorConfig();
        DatabaseProperties dbProps = new DatabaseProperties();
        UidGenerator uidGenerator = Mockito.mock(UidGenerator.class);
        DefGenTable genTable = new DefGenTable();
        genTable.setEntitySuperClass(EntitySuperClassEnum.ENTITY);

        // 1. 无主键
        DefGenTableColumn col1 = new DefGenTableColumn();
        col1.setName("name");
        col1.setIsPk(false);
        assertThrows(RuntimeException.class, () ->
                SourceCodeUtils.getObjectMap(config, dbProps, uidGenerator, null, genTable, List.of(col1), DbType.MYSQL));

        // 2. 复合主键 (>1 个主键)
        DefGenTableColumn pk1 = new DefGenTableColumn();
        pk1.setName("id1");
        pk1.setIsPk(true);
        DefGenTableColumn pk2 = new DefGenTableColumn();
        pk2.setName("id2");
        pk2.setIsPk(true);
        assertThrows(RuntimeException.class, () ->
                SourceCodeUtils.getObjectMap(config, dbProps, uidGenerator, null, genTable, List.of(pk1, pk2), DbType.MYSQL));
    }
}
