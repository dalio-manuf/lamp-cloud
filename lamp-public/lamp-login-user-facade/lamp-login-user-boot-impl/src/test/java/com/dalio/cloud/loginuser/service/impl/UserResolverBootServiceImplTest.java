package com.dalio.cloud.loginuser.service.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.entity.user.BasePosition;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.service.user.BasePositionService;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.oauth.biz.ResourceBiz;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.tenant.DefUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserResolverBootServiceImplTest {

    @Mock
    private DefUserService defUserService;

    @Mock
    private BaseEmployeeService baseEmployeeService;

    @Mock
    private BaseRoleService baseRoleService;

    @Mock
    private BaseOrgService baseOrgService;

    @Mock
    private BasePositionService basePositionService;

    @Mock
    private ResourceBiz resourceBiz;

    @InjectMocks
    private UserResolverBootServiceImpl userResolverBootService;

    @Test
    @DisplayName("测试用户不存在时返回空 SysUser")
    void testGetByIdWhenUserNotFound() {
        UserQuery query = UserQuery.builder().userId(1L).build();
        when(defUserService.getByIdCache(1L)).thenReturn(null);

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNull(result.getData().getId());
    }

    @Test
    @DisplayName("测试员工ID为空或不需要查询员工详情")
    void testGetByIdWithoutEmployee() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(null).build();
        DefUser defUser = new DefUser();
        defUser.setId(1L);
        defUser.setUsername("zhangsan");
        when(defUserService.getByIdCache(1L)).thenReturn(defUser);

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        assertEquals(1L, result.getData().getId());
        assertEquals("zhangsan", result.getData().getUsername());
        assertNull(result.getData().getEmployee());
    }

    @Test
    @DisplayName("测试员工不存在时的分支")
    void testGetByIdEmployeeNotFound() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(10L).full(true).build();
        DefUser defUser = new DefUser();
        defUser.setId(1L);
        when(defUserService.getByIdCache(1L)).thenReturn(defUser);
        when(baseEmployeeService.getByIdCache(10L)).thenReturn(null);

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        assertEquals(1L, result.getData().getId());
        assertNull(result.getData().getEmployee());
    }

    @Test
    @DisplayName("测试完整查询 full=true 成功")
    void testGetByIdFull() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(10L).full(true).build();

        DefUser defUser = new DefUser();
        defUser.setId(1L);
        defUser.setUsername("fullUser");

        BaseEmployee employee = new BaseEmployee();
        employee.setId(10L);
        employee.setLastCompanyId(100L);
        employee.setLastDeptId(200L);
        employee.setPositionId(300L);

        BaseOrg company = new BaseOrg();
        company.setId(100L);
        company.setName("CompanyA");

        BaseOrg dept = new BaseOrg();
        dept.setId(200L);
        dept.setName("DeptB");

        BasePosition position = new BasePosition();
        position.setId(300L);
        position.setName("Engineer");

        when(defUserService.getByIdCache(1L)).thenReturn(defUser);
        when(baseEmployeeService.getByIdCache(10L)).thenReturn(employee);
        when(baseOrgService.getByIdCache(100L)).thenReturn(company);
        when(baseOrgService.getByIdCache(200L)).thenReturn(dept);
        when(baseOrgService.findCompanyByEmployeeId(10L)).thenReturn(Collections.singletonList(company));
        when(baseOrgService.findDeptByEmployeeId(10L, 100L)).thenReturn(Collections.singletonList(dept));
        when(basePositionService.getById(300L)).thenReturn(position);
        when(resourceBiz.findVisibleResource(10L, null)).thenReturn(List.of("perm1", "perm2"));
        when(baseRoleService.findRoleCodeByEmployeeId(10L)).thenReturn(List.of("role1"));

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        SysUser user = result.getData();
        assertEquals(1L, user.getId());
        assertEquals("fullUser", user.getUsername());
        assertNotNull(user.getEmployee());
        assertEquals(10L, user.getEmployee().getId());
        assertNotNull(user.getCompany());
        assertEquals("CompanyA", user.getCompany().getName());
        assertNotNull(user.getDept());
        assertEquals("DeptB", user.getDept().getName());
        assertNotNull(user.getPosition());
        assertEquals("Engineer", user.getPosition().getName());
        assertEquals(List.of("perm1", "perm2"), user.getResourceCodeList());
        assertEquals(List.of("role1"), user.getRoleCodeList());
    }

    @Test
    @DisplayName("测试特定标志位查询与ID为空分支")
    void testGetByIdIndividualFlagsWithEmptyIds() {
        UserQuery query = UserQuery.builder()
                .userId(1L)
                .employeeId(10L)
                .full(false)
                .employee(true)
                .org(false)
                .currentOrg(true)
                .position(true)
                .resource(false)
                .roles(false)
                .build();

        DefUser defUser = new DefUser();
        defUser.setId(1L);

        BaseEmployee employee = new BaseEmployee();
        employee.setId(10L);
        employee.setLastCompanyId(0L); // 0L covers notEmpty == false
        employee.setLastDeptId(null);
        employee.setPositionId(0L);

        when(defUserService.getByIdCache(1L)).thenReturn(defUser);
        when(baseEmployeeService.getByIdCache(10L)).thenReturn(employee);

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        assertNotNull(result.getData().getEmployee());
        assertNull(result.getData().getCompany());
        assertNull(result.getData().getDept());
        assertNull(result.getData().getPosition());
    }

    @Test
    @DisplayName("测试 anyQuery 为 false 时不查询任何员工信息")
    void testGetByIdAnyQueryFalse() {
        UserQuery query = UserQuery.builder()
                .userId(1L)
                .employeeId(10L)
                .full(false)
                .build();

        DefUser defUser = new DefUser();
        defUser.setId(1L);
        when(defUserService.getByIdCache(1L)).thenReturn(defUser);

        R<SysUser> result = userResolverBootService.getById(query);
        assertNotNull(result);
        assertNull(result.getData().getEmployee());
    }
}
