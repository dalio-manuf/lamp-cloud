package com.dalio.cloud.base.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.base.biz.user.BaseEmployeeBiz;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.entity.user.BasePosition;
import com.dalio.cloud.base.service.BaseEmployeeTestService;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.service.user.BasePositionService;
import com.dalio.cloud.base.vo.query.user.BasePositionPageQuery;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;
import com.dalio.cloud.base.vo.result.user.BaseOrgResultVO;
import com.dalio.cloud.base.vo.save.user.BaseEmployeeSaveVO;
import com.dalio.cloud.test.BaseEmployeeTestController;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户相关控制器单元测试
 *
 * @author went
 */
class BaseUserControllersTest {

    @Mock
    private EchoService echoService;

    @Mock
    private BaseEmployeeService employeeService;

    @Mock
    private BaseEmployeeBiz employeeBiz;

    @Mock
    private BaseOrgService orgService;

    @Mock
    private BasePositionService positionService;

    @Mock
    private BaseEmployeeTestService employeeTestService;

    @InjectMocks
    private BaseEmployeeController employeeController;

    @InjectMocks
    private BaseOrgController orgController;

    @InjectMocks
    private BasePositionController positionController;

    @InjectMocks
    private BaseEmployeeTestController employeeTestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(employeeController, "superService", employeeService);
        ReflectionTestUtils.setField(orgController, "superService", orgService);
        ReflectionTestUtils.setField(positionController, "superService", positionService);
    }

    @Test
    @DisplayName("测试 BaseEmployeeController 接口")
    void testEmployeeController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();

        when(employeeBiz.getEmployeeUserById(anyLong())).thenReturn(new BaseEmployeeResultVO());
        when(employeeBiz.findPageResultVO(any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        when(employeeService.findEmployeeRoleByEmployeeId(anyLong())).thenReturn(Collections.singletonList(1L));
        when(employeeService.saveEmployeeRole(any())).thenReturn(Collections.singletonList(1L));

        mockMvc.perform(post("/baseEmployee/page")
                        .contentType("application/json")
                        .content("{\"model\":{}}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/baseEmployee/10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/baseEmployee/findEmployeeRoleByEmployeeId").param("employeeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(1));

        mockMvc.perform(post("/baseEmployee/employeeRole")
                        .contentType("application/json")
                        .content("{\"employeeId\":10,\"roleIdList\":[1,2]}"))
                .andExpect(status().isOk());

        assertNotNull(employeeController.getEchoService());

        BaseEmployeeSaveVO saveVO = new BaseEmployeeSaveVO();
        saveVO.setRealName("张三");
        BaseEmployee emp = new BaseEmployee();
        emp.setId(10L);
        when(employeeBiz.save(saveVO)).thenReturn(emp);
        assertNotNull(employeeController.handlerSave(saveVO));
    }

    @Test
    @DisplayName("测试 BaseOrgController 接口")
    void testOrgController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(orgController).build();

        when(orgService.check(anyString(), anyLong(), any())).thenReturn(true);
        when(orgService.list(any(com.dalio.cloud.base.vo.query.user.BaseOrgPageQuery.class))).thenReturn(Collections.emptyList());
        when(orgService.saveOrgRole(any())).thenReturn(Collections.singletonList(1L));
        when(orgService.findOrgRoleByOrgId(anyLong())).thenReturn(Collections.singletonList(1L));
        when(orgService.findCompanyByEmployeeId(anyLong())).thenReturn(Collections.emptyList());
        when(orgService.findDeptByEmployeeId(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/baseOrg/check").param("name", "研发部").param("parentId", "0").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/baseOrg/tree")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/baseOrg/orgRole")
                        .contentType("application/json")
                        .content("{\"orgId\":1,\"roleIdList\":[10]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/baseOrg/findOrgRoleByOrgId").param("orgId", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/baseOrg/findCompanyByEmployeeId").param("employeeId", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/baseOrg/findDeptByEmployeeId").param("employeeId", "10").param("companyId", "1"))
                .andExpect(status().isOk());

        assertNotNull(orgController.getEchoService());
    }

    @Test
    @DisplayName("测试 BasePositionController 接口与 QueryWrap 处理")
    void testPositionController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(positionController).build();

        when(positionService.check(anyString(), anyLong(), any())).thenReturn(true);

        mockMvc.perform(get("/basePosition/check").param("name", "经理").param("orgId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        assertNotNull(positionController.getEchoService());

        // 测试 handlerWrapper
        PageParams<BasePositionPageQuery> params = new PageParams<>();
        BasePositionPageQuery query = new BasePositionPageQuery();
        query.setOrgIdList(Collections.singletonList(1L));
        params.setModel(query);
        assertNotNull(positionController.handlerWrapper(new BasePosition(), params));
    }

    @Test
    @DisplayName("测试 BaseEmployeeTestController 接口")
    void testEmployeeTestController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(employeeTestController).build();

        BaseEmployee emp = new BaseEmployee();
        emp.setId(10L);
        when(employeeTestService.save(any())).thenReturn(true);
        when(employeeTestService.getById(eq(10L))).thenReturn(emp);
        when(employeeTestService.get(eq(10L))).thenReturn(emp);

        mockMvc.perform(post("/baseEmployeeController/save")
                        .contentType("application/json")
                        .content("{\"id\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/baseEmployeeController/getById").param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));

        mockMvc.perform(get("/baseEmployeeController/get").param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }
}
