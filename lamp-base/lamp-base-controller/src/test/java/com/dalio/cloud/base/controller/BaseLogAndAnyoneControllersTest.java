package com.dalio.cloud.base.controller;

import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.base.controller.anyone.BaseAnyoneController;
import com.dalio.cloud.base.controller.system.BaseLoginLogController;
import com.dalio.cloud.base.controller.system.BaseOperationLogController;
import com.dalio.cloud.base.service.system.BaseOperationLogService;
import com.dalio.cloud.base.vo.result.system.BaseOperationLogResultVO;
import com.dalio.cloud.system.service.system.DefLoginLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 日志与公开控制器单元测试
 *
 * @author went
 */
class BaseLogAndAnyoneControllersTest {

    @Mock
    private EchoService echoService;

    @Mock
    private BaseOperationLogService operationLogService;

    @Mock
    private DefLoginLogService loginLogService;

    @InjectMocks
    private BaseOperationLogController operationLogController;

    @InjectMocks
    private BaseLoginLogController loginLogController;

    @InjectMocks
    private BaseAnyoneController anyoneController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(operationLogController, "superService", operationLogService);
        ReflectionTestUtils.setField(loginLogController, "superService", loginLogService);
    }

    @Test
    @DisplayName("测试 BaseOperationLogController 接口及清空日志分支")
    void testOperationLogController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(operationLogController).build();

        BaseOperationLogResultVO detail = new BaseOperationLogResultVO();
        detail.setId(10L);
        when(operationLogService.getDetail(anyLong())).thenReturn(detail);
        when(operationLogService.clearLog(any(), any())).thenReturn(true);

        mockMvc.perform(get("/baseOperationLog/detail").param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));

        // 覆盖 clear 的各种类型分支 (1-4 按时间, 5-8 按数量, default)
        for (int t = 1; t <= 9; t++) {
            mockMvc.perform(delete("/baseOperationLog/clear").param("type", String.valueOf(t)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }

        assertNotNull(operationLogController.getEchoService());
    }

    @Test
    @DisplayName("测试 BaseLoginLogController 接口及清空日志分支")
    void testLoginLogController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(loginLogController).build();

        when(loginLogService.clearLog(any(), any())).thenReturn(true);

        for (int t = 1; t <= 9; t++) {
            mockMvc.perform(delete("/baseLoginLog/clear").param("type", String.valueOf(t)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }

        assertNotNull(loginLogController.getEchoService());
    }

    @Test
    @DisplayName("测试 BaseAnyoneController 公开接口")
    void testAnyoneController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(anyoneController).build();

        mockMvc.perform(get("/anyone/base/test"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/anyone/base/test").param("id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }
}
