package com.dalio.cloud.loginuser.service.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.loginuser.api.BaseApi;
import com.dalio.cloud.loginuser.api.OauthApi;
import com.dalio.cloud.loginuser.api.SystemApi;
import com.dalio.cloud.model.entity.base.SysEmployee;
import com.dalio.cloud.model.entity.base.SysOrg;
import com.dalio.cloud.model.entity.base.SysPosition;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserResolverServiceImplTest {

    @Mock
    private BaseApi baseApi;

    @Mock
    private OauthApi oauthApi;

    @Mock
    private SystemApi systemApi;

    @InjectMocks
    private UserResolverServiceImpl userResolverService;

    @Test
    @DisplayName("测试用户查询失败或数据为空时返回空 SysUser")
    void testGetByIdWhenUserNotFound() {
        UserQuery query = UserQuery.builder().userId(1L).build();
        when(systemApi.getUserById(1L)).thenReturn(R.fail("用户不存在"));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNull(result.getData().getId());

        when(systemApi.getUserById(1L)).thenReturn(R.success(null));
        result = userResolverService.getById(query);
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNull(result.getData().getId());
    }

    @Test
    @DisplayName("测试无需查询员工详情的分支")
    void testGetByIdWithoutEmployee() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(null).build();
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("user1");
        when(systemApi.getUserById(1L)).thenReturn(R.success(sysUser));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        assertEquals(1L, result.getData().getId());
        assertEquals("user1", result.getData().getUsername());
        assertNull(result.getData().getEmployee());
    }

    @Test
    @DisplayName("测试员工查询失败分支")
    void testGetByIdEmployeeNotFound() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(10L).full(true).build();
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        when(systemApi.getUserById(1L)).thenReturn(R.success(sysUser));
        when(baseApi.getEmployeeById(10L)).thenReturn(R.fail("员工不存在"));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        assertEquals(1L, result.getData().getId());
        assertNull(result.getData().getEmployee());

        when(baseApi.getEmployeeById(10L)).thenReturn(R.success(null));
        result = userResolverService.getById(query);
        assertNotNull(result);
        assertNull(result.getData().getEmployee());
    }

    @Test
    @DisplayName("测试完整查询 full=true 成功")
    void testGetByIdFullSuccess() {
        UserQuery query = UserQuery.builder().userId(1L).employeeId(10L).full(true).build();

        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("fullCloudUser");

        SysEmployee employee = new SysEmployee();
        employee.setId(10L);
        employee.setLastCompanyId(100L);
        employee.setLastDeptId(200L);
        employee.setPositionId(300L);

        SysOrg company = new SysOrg();
        company.setId(100L);
        company.setName("CompanyCloud");

        SysOrg dept = new SysOrg();
        dept.setId(200L);
        dept.setName("DeptCloud");

        SysPosition position = new SysPosition();
        position.setId(300L);
        position.setName("Architect");

        when(systemApi.getUserById(1L)).thenReturn(R.success(sysUser));
        when(baseApi.getEmployeeById(10L)).thenReturn(R.success(employee));
        when(baseApi.getOrgById(100L)).thenReturn(R.success(company));
        when(baseApi.getOrgById(200L)).thenReturn(R.success(dept));
        when(baseApi.findCompanyByEmployeeId(10L)).thenReturn(R.success(Collections.singletonList(company)));
        when(baseApi.findDeptByEmployeeId(10L, 100L)).thenReturn(R.success(Collections.singletonList(dept)));
        when(baseApi.getPositionById(300L)).thenReturn(R.success(position));
        when(oauthApi.findVisibleResource(10L, null)).thenReturn(R.success(List.of("btn:add", "btn:edit")));
        when(baseApi.findRoleCodeByEmployeeId(10L)).thenReturn(R.success(List.of("admin_role")));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        SysUser user = result.getData();
        assertEquals(1L, user.getId());
        assertEquals("fullCloudUser", user.getUsername());
        assertNotNull(user.getEmployee());
        assertEquals(10L, user.getEmployee().getId());
        assertNotNull(user.getCompany());
        assertEquals("CompanyCloud", user.getCompany().getName());
        assertNotNull(user.getDept());
        assertEquals("DeptCloud", user.getDept().getName());
        assertNotNull(user.getPosition());
        assertEquals("Architect", user.getPosition().getName());
        assertEquals(List.of("btn:add", "btn:edit"), user.getResourceCodeList());
        assertEquals(List.of("admin_role"), user.getRoleCodeList());
    }

    @Test
    @DisplayName("测试独立标志位查询与ID为空分支")
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

        SysUser sysUser = new SysUser();
        sysUser.setId(1L);

        SysEmployee employee = new SysEmployee();
        employee.setId(10L);
        employee.setLastCompanyId(0L);
        employee.setLastDeptId(null);
        employee.setPositionId(0L);

        when(systemApi.getUserById(1L)).thenReturn(R.success(sysUser));
        when(baseApi.getEmployeeById(10L)).thenReturn(R.success(employee));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        assertNotNull(result.getData().getEmployee());
        assertNull(result.getData().getCompany());
        assertNull(result.getData().getDept());
        assertNull(result.getData().getPosition());
    }

    @Test
    @DisplayName("测试 anyQuery 为 false 时不查询员工")
    void testGetByIdAnyQueryFalse() {
        UserQuery query = UserQuery.builder()
                .userId(1L)
                .employeeId(10L)
                .full(false)
                .build();

        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        when(systemApi.getUserById(1L)).thenReturn(R.success(sysUser));

        R<SysUser> result = userResolverService.getById(query);
        assertNotNull(result);
        assertNull(result.getData().getEmployee());
    }
}
