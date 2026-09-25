package com.dalio.cloud.datascope.service;

import com.dalio.cloud.datascope.entity.BaseOrgBO;
import com.dalio.cloud.datascope.entity.DefResourceDataScope;
import com.dalio.cloud.datascope.mapper.DataScopeMapper;
import com.dalio.cloud.model.enumeration.base.OrgTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DataScopeService 与 OrgHelperService 单元测试
 */
class DataScopeAndOrgHelperServiceTest {

    private DataScopeMapper dataScopeMapper;
    private DataScopeService dataScopeService;
    private OrgHelperService orgHelperService;

    @BeforeEach
    void setUp() {
        dataScopeMapper = Mockito.mock(DataScopeMapper.class);
        dataScopeService = new DataScopeService(dataScopeMapper);
        orgHelperService = new OrgHelperService(dataScopeMapper);
    }

    @Test
    @DisplayName("测试 getDataScopeByPath 自定义权限与默认权限回退")
    void testGetDataScopeByPath() {
        Long appId = 1L;
        String path = "/api/users";
        List<Long> dataScopeIds = Collections.singletonList(10L);

        DefResourceDataScope scope1 = new DefResourceDataScope();
        scope1.setId(100L);

        // 1. 员工有权限，直接命中
        when(dataScopeMapper.findDataScopeByPath(appId, path, dataScopeIds)).thenReturn(Collections.singletonList(scope1));
        DefResourceDataScope result1 = dataScopeService.getDataScopeByPath(appId, path, dataScopeIds);
        assertNotNull(result1);
        assertEquals(100L, result1.getId());

        // 2. 员工无权限，回退默认权限
        when(dataScopeMapper.findDataScopeByPath(appId, path, Collections.emptyList())).thenReturn(Collections.emptyList());
        when(dataScopeMapper.findDefDataScopeByPath(appId, path)).thenReturn(Collections.singletonList(scope1));
        DefResourceDataScope result2 = dataScopeService.getDataScopeByPath(appId, path, Collections.emptyList());
        assertNotNull(result2);
        assertEquals(100L, result2.getId());

        // 3. 既无权限也无默认权限
        when(dataScopeMapper.findDefDataScopeByPath(appId, "/unknown")).thenReturn(Collections.emptyList());
        assertNull(dataScopeService.getDataScopeByPath(appId, "/unknown", Collections.emptyList()));
    }

    @Test
    @DisplayName("测试 selectDataScopeIdByEmployeeId 汇总角色与机构权限")
    void testSelectDataScopeIdByEmployeeId() {
        Long empId = 50L;
        String category = "CAT";

        when(dataScopeMapper.selectDataScopeIdFromRoleByEmployeeId(empId, category)).thenReturn(Arrays.asList(1L, 2L));
        when(dataScopeMapper.selectDataScopeIdFromOrgByEmployeeId(empId, category)).thenReturn(Arrays.asList(2L, 3L));

        List<Long> ids = dataScopeService.selectDataScopeIdByEmployeeId(empId, category);
        assertNotNull(ids);
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    @DisplayName("测试 OrgHelperService 获取部门和单位逻辑")
    void testOrgHelperService() {
        Long empId = 88L;

        // 部门为空
        when(dataScopeMapper.getMainDeptIdByEmployeeId(empId)).thenReturn(null);
        assertNull(orgHelperService.getMainDeptIdByEmployeeId(empId));
        assertTrue(orgHelperService.findDeptAndChildrenIdByEmployeeId(empId).isEmpty());
        assertNull(orgHelperService.getMainCompanyIdByEmployeeId(empId));
        assertTrue(orgHelperService.findCompanyAndChildrenIdByEmployeeId(empId).isEmpty());

        // 部门直接为单位
        BaseOrgBO companyOrg = new BaseOrgBO();
        companyOrg.setId(10L);
        companyOrg.setType(OrgTypeEnum.COMPANY.getCode());
        companyOrg.setTreePath(",10,");

        when(dataScopeMapper.getMainDeptIdByEmployeeId(empId)).thenReturn(companyOrg);
        when(dataScopeMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertEquals(10L, orgHelperService.getMainDeptIdByEmployeeId(empId));
        assertEquals(Collections.singletonList(10L), orgHelperService.findDeptAndChildrenIdByEmployeeId(empId));
        assertEquals(10L, orgHelperService.getMainCompanyIdByEmployeeId(empId));
        assertEquals(Collections.singletonList(10L), orgHelperService.findCompanyAndChildrenIdByEmployeeId(empId));

        // 部门为 DEPT，向上递归找 COMPANY
        BaseOrgBO deptOrg = new BaseOrgBO();
        deptOrg.setId(20L);
        deptOrg.setParentId(10L);
        deptOrg.setType(OrgTypeEnum.DEPT.getCode());
        deptOrg.setTreePath(com.dalio.cloud.common.constant.DefValConstants.TREE_PATH_SPLIT + "10" + com.dalio.cloud.common.constant.DefValConstants.TREE_PATH_SPLIT + "20" + com.dalio.cloud.common.constant.DefValConstants.TREE_PATH_SPLIT);

        when(dataScopeMapper.getMainDeptIdByEmployeeId(empId)).thenReturn(deptOrg);
        when(dataScopeMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(companyOrg));

        assertEquals(10L, orgHelperService.getMainCompanyIdByEmployeeId(empId));
    }
}
