package com.dalio.cloud.base.controller.system;

import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.base.biz.system.BaseRoleBiz;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.base.vo.query.system.BaseRolePageQuery;
import com.dalio.cloud.model.enumeration.base.RoleCategoryEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BaseRoleController 单元测试
 *
 * @author went
 */
class BaseRoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BaseRoleService baseRoleService;

    @Mock
    private BaseRoleBiz baseRoleBiz;

    @Mock
    private EchoService echoService;

    @InjectMocks
    private BaseRoleController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(controller, "superService", baseRoleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试 check 角色编码")
    void testCheck() throws Exception {
        when(baseRoleService.check(anyString(), any())).thenReturn(true);

        mockMvc.perform(get("/baseRole/check").param("code", "ADMIN").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(baseRoleService).check("ADMIN", 1L);
    }

    @Test
    @DisplayName("测试 roleEmployee 分配员工")
    void testSaveRoleEmployee() throws Exception {
        when(baseRoleService.saveRoleEmployee(any())).thenReturn(Collections.singletonList(1L));

        mockMvc.perform(post("/baseRole/roleEmployee")
                        .contentType("application/json")
                        .content("{\"roleId\":1,\"employeeIdList\":[10,20]}"))
                .andExpect(status().isOk());

        verify(baseRoleService).saveRoleEmployee(any());
    }

    @Test
    @DisplayName("测试 roleResource 配置资源")
    void testSaveRoleResource() throws Exception {
        when(baseRoleService.saveRoleResource(any())).thenReturn(true);

        mockMvc.perform(post("/baseRole/roleResource")
                        .contentType("application/json")
                        .content("{\"roleId\":1,\"resourceIdList\":[100,200]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(baseRoleService).saveRoleResource(any());
    }

    @Test
    @DisplayName("测试 employeeList 查询角色的员工")
    void testFindEmployeeIdByRoleId() throws Exception {
        when(baseRoleService.findEmployeeIdByRoleId(anyLong())).thenReturn(Collections.singletonList(10L));

        mockMvc.perform(get("/baseRole/employeeList").param("roleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(10));

        verify(baseRoleService).findEmployeeIdByRoleId(1L);
    }

    @Test
    @DisplayName("测试 resourceList 查询角色的资源")
    void testFindResourceIdByRoleId() throws Exception {
        when(baseRoleBiz.findResourceIdByRoleId(anyLong())).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/baseRole/resourceList").param("roleId", "1"))
                .andExpect(status().isOk());

        verify(baseRoleBiz).findResourceIdByRoleId(1L);
    }

    @Test
    @DisplayName("测试 findResourceDataScopeIdByRoleId 查询角色数据权限")
    void testFindResourceDataScopeIdByRoleId() throws Exception {
        when(baseRoleService.findResourceIdByRoleId(eq(1L), eq(RoleCategoryEnum.DATA_SCOPE))).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/baseRole/findResourceDataScopeIdByRoleId").param("roleId", "1"))
                .andExpect(status().isOk());

        verify(baseRoleService).findResourceIdByRoleId(1L, RoleCategoryEnum.DATA_SCOPE);
    }

    @Test
    @DisplayName("测试 findRoleCodeByEmployeeId 查询员工拥有的角色编码")
    void testFindRoleCodeByEmployeeId() throws Exception {
        when(baseRoleService.findRoleCodeByEmployeeId(anyLong())).thenReturn(Collections.singletonList("ADMIN"));

        mockMvc.perform(get("/baseRole/findRoleCodeByEmployeeId").param("employeeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("ADMIN"));

        verify(baseRoleService).findRoleCodeByEmployeeId(10L);
    }

    @Test
    @DisplayName("测试 handlerWrapper 和 getEchoService")
    void testHandlerWrapper() {
        assertNotNull(controller.getEchoService());

        BaseRole model = new BaseRole();
        model.setCategory("10");
        model.setState(true);
        model.setName("admin");

        PageParams<BaseRolePageQuery> params = new PageParams<>();
        params.setModel(new BaseRolePageQuery());

        assertNotNull(controller.handlerWrapper(model, params));
    }

    @Test
    @DisplayName("测试 pageMyRole 分页查询员工或组织绑定的角色")
    void testPageMyRole() throws Exception {
        // scopeType=EMPLOYEE, scope=BIND
        mockMvc.perform(post("/baseRole/pageMyRole")
                        .contentType("application/json")
                        .content("{\"model\":{\"scopeType\":\"1\",\"scope\":\"1\",\"employeeId\":10}}"))
                .andExpect(status().isOk());

        // scopeType=EMPLOYEE, scope=UN_BIND
        mockMvc.perform(post("/baseRole/pageMyRole")
                        .contentType("application/json")
                        .content("{\"model\":{\"scopeType\":\"1\",\"scope\":\"2\",\"employeeId\":10}}"))
                .andExpect(status().isOk());

        // scopeType=ORG, scope=BIND
        mockMvc.perform(post("/baseRole/pageMyRole")
                        .contentType("application/json")
                        .content("{\"model\":{\"scopeType\":\"2\",\"scope\":\"1\",\"orgId\":20}}"))
                .andExpect(status().isOk());

        // scopeType=ORG, scope=UN_BIND
        mockMvc.perform(post("/baseRole/pageMyRole")
                        .contentType("application/json")
                        .content("{\"model\":{\"scopeType\":\"2\",\"scope\":\"2\",\"orgId\":20}}"))
                .andExpect(status().isOk());
    }
}
