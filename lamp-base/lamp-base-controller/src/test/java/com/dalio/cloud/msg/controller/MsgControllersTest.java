package com.dalio.cloud.msg.controller;

import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.service.ExtendInterfaceLogService;
import com.dalio.cloud.msg.service.ExtendInterfaceLoggingService;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.dalio.cloud.msg.service.ExtendNoticeService;
import com.dalio.cloud.msg.vo.query.ExtendMsgPageQuery;
import com.dalio.cloud.msg.vo.query.ExtendNoticePageQuery;
import com.dalio.cloud.msg.vo.result.ExtendMsgResultVO;
import com.dalio.cloud.msg.ws.TestEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 消息相关控制器单元测试
 *
 * @author went
 */
class MsgControllersTest {

    @Mock
    private EchoService echoService;

    @Mock
    private MsgBiz msgBiz;

    @Mock
    private ExtendMsgService extendMsgService;

    @Mock
    private ExtendNoticeService extendNoticeService;

    @Mock
    private ExtendInterfaceLogService interfaceLogService;

    @Mock
    private ExtendInterfaceLoggingService interfaceLoggingService;

    @InjectMocks
    private MsgController msgController;

    @InjectMocks
    private ExtendMsgController extendMsgController;

    @InjectMocks
    private ExtendNoticeController extendNoticeController;

    @InjectMocks
    private ExtendInterfaceLogController interfaceLogController;

    @InjectMocks
    private ExtendInterfaceLoggingController interfaceLoggingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(extendMsgController, "superService", extendMsgService);
        ReflectionTestUtils.setField(extendNoticeController, "superService", extendNoticeService);
        ReflectionTestUtils.setField(interfaceLogController, "superService", interfaceLogService);
        ReflectionTestUtils.setField(interfaceLoggingController, "superService", interfaceLoggingService);
    }

    @Test
    @DisplayName("测试 MsgController 发送消息")
    void testMsgController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(msgController).build();

        when(msgBiz.sendByTemplate(any(), any())).thenReturn(true);

        mockMvc.perform(post("/anyUser/extendMsg/sendByTemplate")
                        .contentType("application/json")
                        .content("{\"code\":\"TMPL_01\",\"recipientList\":[{\"recipient\":\"13800000000\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试 ExtendMsgController 接口")
    void testExtendMsgController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(extendMsgController).build();

        when(msgBiz.sendByTemplate(any(), any())).thenReturn(true);
        when(msgBiz.publish(any(), any())).thenReturn(true);
        when(extendMsgService.getResultById(anyLong())).thenReturn(new ExtendMsgResultVO());

        mockMvc.perform(post("/extendMsg/sendByTemplate")
                        .contentType("application/json")
                        .content("{\"code\":\"TMPL_01\",\"recipientList\":[{\"recipient\":\"13800000000\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/extendMsg/publish")
                        .contentType("application/json")
                        .content("{\"title\":\"公告\",\"recipientList\":[\"user1\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/extendMsg/10"))
                .andExpect(status().isOk());

        assertNotNull(extendMsgController.getEchoService());

        // handlerWrapper
        PageParams<ExtendMsgPageQuery> params = new PageParams<>();
        params.setModel(new ExtendMsgPageQuery());
        assertNotNull(extendMsgController.handlerWrapper(new ExtendMsg(), params));
    }

    @Test
    @DisplayName("测试 ExtendNoticeController 标记与删除")
    void testExtendNoticeController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(extendNoticeController).build();

        when(extendNoticeService.mark(any(), any())).thenReturn(true);
        when(extendNoticeService.deleteMyNotice(any())).thenReturn(true);

        mockMvc.perform(post("/anyone/extendNotice/mark")
                        .contentType("application/json")
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(delete("/anyone/extendNotice/deleteMyNotice")
                        .contentType("application/json")
                        .content("[1,2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/anyone/extendNotice/myNotice")
                        .contentType("application/json")
                        .content("{\"model\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertNotNull(extendNoticeController.getEchoService());

        PageParams<ExtendNoticePageQuery> params = new PageParams<>();
        params.setModel(new ExtendNoticePageQuery());
        extendNoticeController.handlerQueryParams(params);
    }

    @Test
    @DisplayName("测试 ExtendInterfaceLogController & ExtendInterfaceLoggingController")
    void testLogControllers() {
        assertNotNull(interfaceLogController.getEchoService());
        assertNotNull(interfaceLoggingController.getEchoService());
    }

    @Test
    @DisplayName("测试 TestEndpoint WebSocket 处理器")
    void testTestEndpoint() throws Exception {
        TestEndpoint endpoint = new TestEndpoint();
        assertEquals("", endpoint.onMsg(""));
        assertEquals("server 收到消息：hello", endpoint.onMsg("hello"));
        endpoint.onError(null, new RuntimeException("test"));

        jakarta.websocket.Session mockSession = org.mockito.Mockito.mock(jakarta.websocket.Session.class);
        when(mockSession.getId()).thenReturn("s123");
        when(mockSession.isOpen()).thenReturn(true);

        endpoint.onOpen(mockSession);
        endpoint.onClose(mockSession);
    }

    @Test
    @DisplayName("测试 MsgEndpoint 接收消息与生命周期")
    void testMsgEndpoint() throws Exception {
        com.dalio.cloud.msg.ws.MsgEndpoint endpoint = new com.dalio.cloud.msg.ws.MsgEndpoint();
        assertEquals("", endpoint.onMsg("1", ""));
        assertEquals("", endpoint.onMsg("1", "ping"));
        endpoint.onError(null, new RuntimeException("test"));

        jakarta.websocket.Session mockSession = org.mockito.Mockito.mock(jakarta.websocket.Session.class);
        when(mockSession.getId()).thenReturn("s456");
        when(mockSession.isOpen()).thenReturn(true);

        endpoint.onOpen("emp1", mockSession);
        endpoint.onClose("emp1", mockSession);
    }
}
