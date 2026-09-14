package com.dalio.cloud.datascope.provider;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.datascope.service.OrgHelperService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * DataScopeContext 及各类 DataScopeProvider 单元测试
 */
class DataScopeContextAndProvidersTest {

    private OrgHelperService orgHelperService;

    @BeforeEach
    void setUp() {
        orgHelperService = Mockito.mock(OrgHelperService.class);
        ContextUtil.setUserId(100L);
        ContextUtil.setEmployeeId(200L);
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试 DataScopeContext 获取 Provider 及异常分支")
    void testDataScopeContext() {
        DataScopeProvider mockProvider = Mockito.mock(DataScopeProvider.class);
        Map<String, DataScopeProvider> map = new HashMap<>();
        map.put("DATA_SCOPE_01", mockProvider);

        DataScopeContext context = new DataScopeContext(map);
        assertSame(mockProvider, context.getDataScopeProvider("01"));
        assertSame(mockProvider, context.getDataScopeProvider("DATA_SCOPE_01"));

        assertThrows(BizException.class, () -> context.getDataScopeProvider("99"));
    }

    @Test
    @DisplayName("测试 AllDataScopeProviderImpl")
    void testAllDataScopeProvider() {
        AllDataScopeProviderImpl provider = new AllDataScopeProviderImpl();
        List<DataFieldProperty> list = new ArrayList<>(Collections.singletonList(new DataFieldProperty("t")));
        List<DataFieldProperty> result = provider.findDataFieldProperty(list);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试 SelfDataScopeProviderImpl")
    void testSelfDataScopeProvider() {
        SelfDataScopeProviderImpl provider = new SelfDataScopeProviderImpl();
        DataFieldProperty prop = new DataFieldProperty("t");
        List<DataFieldProperty> list = new ArrayList<>(Collections.singletonList(prop));

        List<DataFieldProperty> result = provider.findDataFieldProperty(list);
        assertFalse(result.isEmpty());
        assertEquals(SuperEntity.CREATED_BY_FIELD, result.get(0).getField());
        assertEquals(Collections.singletonList(100L), result.get(0).getValues());
    }

    @Test
    @DisplayName("测试 CompanyDataScopeProviderImpl 正常与空数据分支")
    void testCompanyDataScopeProvider() {
        CompanyDataScopeProviderImpl provider = new CompanyDataScopeProviderImpl(orgHelperService);

        // 空数据
        when(orgHelperService.getMainCompanyIdByEmployeeId(200L)).thenReturn(null);
        List<DataFieldProperty> emptyResult = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertTrue(emptyResult.isEmpty());

        // 有数据
        when(orgHelperService.getMainCompanyIdByEmployeeId(200L)).thenReturn(500L);
        List<DataFieldProperty> result = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertEquals(SuperEntity.CREATED_ORG_ID_FIELD, result.get(0).getField());
        assertEquals(Collections.singletonList(500L), result.get(0).getValues());
    }

    @Test
    @DisplayName("测试 CompanyChildrenDataScopeProviderImpl 正常与空数据分支")
    void testCompanyChildrenDataScopeProvider() {
        CompanyChildrenDataScopeProviderImpl provider = new CompanyChildrenDataScopeProviderImpl(orgHelperService);

        when(orgHelperService.findCompanyAndChildrenIdByEmployeeId(200L)).thenReturn(Collections.emptyList());
        List<DataFieldProperty> emptyResult = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertTrue(emptyResult.isEmpty());

        when(orgHelperService.findCompanyAndChildrenIdByEmployeeId(200L)).thenReturn(Arrays.asList(500L, 501L));
        List<DataFieldProperty> result = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertEquals(SuperEntity.CREATED_ORG_ID_FIELD, result.get(0).getField());
        assertEquals(Arrays.asList(500L, 501L), result.get(0).getValues());
    }

    @Test
    @DisplayName("测试 DeptDataScopeProviderImpl 正常与空数据分支")
    void testDeptDataScopeProvider() {
        DeptDataScopeProviderImpl provider = new DeptDataScopeProviderImpl(orgHelperService);

        when(orgHelperService.getMainDeptIdByEmployeeId(200L)).thenReturn(null);
        List<DataFieldProperty> emptyResult = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertTrue(emptyResult.isEmpty());

        when(orgHelperService.getMainDeptIdByEmployeeId(200L)).thenReturn(600L);
        List<DataFieldProperty> result = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertEquals(SuperEntity.CREATED_ORG_ID_FIELD, result.get(0).getField());
        assertEquals(Collections.singletonList(600L), result.get(0).getValues());
    }

    @Test
    @DisplayName("测试 DeptChildrenDataScopeProviderImpl 正常与空数据分支")
    void testDeptChildrenDataScopeProvider() {
        DeptChildrenDataScopeProviderImpl provider = new DeptChildrenDataScopeProviderImpl(orgHelperService);

        when(orgHelperService.findDeptAndChildrenIdByEmployeeId(200L)).thenReturn(Collections.emptyList());
        List<DataFieldProperty> emptyResult = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertTrue(emptyResult.isEmpty());

        when(orgHelperService.findDeptAndChildrenIdByEmployeeId(200L)).thenReturn(Arrays.asList(600L, 601L));
        List<DataFieldProperty> result = provider.findDataFieldProperty(new ArrayList<>(Collections.singletonList(new DataFieldProperty("t"))));
        assertEquals(SuperEntity.CREATED_ORG_ID_FIELD, result.get(0).getField());
        assertEquals(Arrays.asList(600L, 601L), result.get(0).getValues());
    }
}
