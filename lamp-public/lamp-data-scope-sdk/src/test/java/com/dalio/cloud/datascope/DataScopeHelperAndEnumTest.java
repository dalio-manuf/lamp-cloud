package com.dalio.cloud.datascope;

import com.dalio.cloud.datascope.entity.BaseOrgBO;
import com.dalio.cloud.datascope.entity.DefResourceDataScope;
import com.dalio.cloud.datascope.interceptor.DataScopeInnerInterceptor;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.datascope.model.DataScopeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataScopeHelper 与 DataScopeEnum 单元测试
 */
class DataScopeHelperAndEnumTest {

    @AfterEach
    void tearDown() {
        DataScopeHelper.clearDataScope();
    }

    @Test
    @DisplayName("测试 DataScopeHelper 本地线程变量设置与清理")
    void testDataScopeHelper() {
        assertNull(DataScopeHelper.getLocalDataScope());

        DataScopeHelper.startDataScope();
        assertNull(DataScopeHelper.getLocalDataScope());

        DataScopeHelper.startDataScope("t1", "t2");
        List<DataFieldProperty> localDataScope = DataScopeHelper.getLocalDataScope();
        assertNotNull(localDataScope);
        assertEquals(2, localDataScope.size());
        assertEquals("t1", localDataScope.get(0).getAlias());
        assertEquals("t2", localDataScope.get(1).getAlias());

        DataScopeHelper.clearDataScope();
        assertNull(DataScopeHelper.getLocalDataScope());
    }

    @Test
    @DisplayName("测试 DataScopeEnum 枚举匹配与方法")
    void testDataScopeEnum() {
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.get("ALL"));
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.get("01"));
        assertEquals(DataScopeEnum.SELF, DataScopeEnum.match("06", null));
        assertEquals(DataScopeEnum.CUSTOM, DataScopeEnum.match("CUSTOM", null));
        assertNull(DataScopeEnum.get(null));
        assertNull(DataScopeEnum.get("NON_EXISTENT"));
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.match("NON_EXISTENT", DataScopeEnum.ALL));

        assertTrue(DataScopeEnum.ALL.eq(DataScopeEnum.ALL));
        assertFalse(DataScopeEnum.ALL.eq(DataScopeEnum.SELF));
        assertEquals("01", DataScopeEnum.ALL.getCode());
        assertEquals("01", DataScopeEnum.ALL.getVal());
        assertEquals("全部", DataScopeEnum.ALL.getDesc());
    }

    @Test
    @DisplayName("测试 DataFieldProperty 属性与方法")
    void testDataFieldProperty() {
        assertNotNull(DataFieldProperty.EMPTY_INSTANCE);

        DataFieldProperty prop = new DataFieldProperty("t");
        prop.setField("org_id");
        prop.setValues(Collections.singletonList(100L));
        assertEquals("t.org_id", prop.getAliasDotField());
        assertEquals(Collections.singletonList(100L), prop.getValues());

        DataFieldProperty propNoAlias = new DataFieldProperty();
        propNoAlias.setField("create_by");
        assertEquals("create_by", propNoAlias.getAliasDotField());

        DataFieldProperty emptyFieldProp = new DataFieldProperty();
        assertThrows(RuntimeException.class, emptyFieldProp::getAliasDotField);
    }

    @Test
    @DisplayName("测试 BaseOrgBO 与 DefResourceDataScope 实体模型")
    void testEntities() {
        LocalDateTime now = LocalDateTime.now();
        BaseOrgBO org = BaseOrgBO.builder()
                .id(1L)
                .name("总部")
                .type("10")
                .shortName("总部")
                .parentId(0L)
                .treeGrade(1)
                .treePath("0/1")
                .sortValue(1)
                .state(true)
                .remarks("测试组织")
                .createdTime(now)
                .createdBy(1L)
                .updatedTime(now)
                .updatedBy(1L)
                .build();

        assertEquals(1L, org.getId());
        assertEquals("总部", org.getName());
        assertEquals("10", org.getType());
        assertEquals(0L, org.getParentId());
        assertTrue(org.getState());

        DefResourceDataScope dataScope = DefResourceDataScope.builder()
                .id(10L)
                .name("本部门权限")
                .dataScope("04")
                .sortValue(1)
                .parentId(100L)
                .customClass("com.test.CustomClass")
                .isDef(true)
                .createdBy(1L)
                .createdTime(now)
                .updatedBy(1L)
                .updatedTime(now)
                .build();

        assertEquals(10L, dataScope.getId());
        assertEquals("本部门权限", dataScope.getName());
        assertEquals("04", dataScope.getDataScope());
        assertTrue(dataScope.getIsDef());

        DataScopeInnerInterceptor interceptor = new DataScopeInnerInterceptor();
        assertNotNull(interceptor);
    }
}
