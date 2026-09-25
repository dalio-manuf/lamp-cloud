package com.dalio.cloud.msg.biz;

import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.msg.entity.*;
import com.dalio.cloud.msg.enumeration.InterfaceExecModeEnum;
import com.dalio.cloud.msg.manager.ExtendInterfaceLogManager;
import com.dalio.cloud.msg.manager.ExtendInterfaceLoggingManager;
import com.dalio.cloud.msg.manager.ExtendMsgManager;
import com.dalio.cloud.msg.service.*;
import com.dalio.cloud.msg.strategy.MsgContext;
import com.dalio.cloud.msg.strategy.MsgStrategy;
import com.dalio.cloud.msg.strategy.domain.MsgResult;
import com.dalio.cloud.msg.vo.update.ExtendMsgPublishVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import com.dalio.cloud.msg.ws.WebSocketObserver;
import com.dalio.cloud.msg.ws.WebSocketSubject;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MsgContext 与 MsgBiz 及 WebSocket 相关测试
 */
class MsgContextAndBizTest {

    @Test
    @DisplayName("测试 MsgContext execSend 策略调用成功与失败流程")
    void testMsgContextExecSend() throws Exception {
        ExtendInterfaceLogManager logManager = mock(ExtendInterfaceLogManager.class);
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendInterfaceLoggingManager loggingManager = mock(ExtendInterfaceLoggingManager.class);

        MsgContext context = new MsgContext(logManager, msgManager, loggingManager);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(100L);
        msg.setBizId(12345L);
        msg.setParam("{\"code\":\"1234\"}");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setId(1L);

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13800000000");

        DefInterface defInterface = new DefInterface();
        defInterface.setId(10L);
        defInterface.setName("短信接口");
        defInterface.setExecMode(InterfaceExecModeEnum.IMPL_CLASS.getCode());
        defInterface.setImplClass("aliSmsMsgStrategyImpl");

        ExtendInterfaceLog existingLog = new ExtendInterfaceLog();
        existingLog.setId(555L);
        when(logManager.getByInterfaceId(10L)).thenReturn(existingLog);

        MsgStrategy mockStrategy = mock(MsgStrategy.class);
        MsgResult successResult = MsgResult.builder().title("验证码").content("1234").result("OK").build();
        when(mockStrategy.exec(any())).thenReturn(successResult);
        when(mockStrategy.isSuccess(any())).thenReturn(true);

        // 1. 测试成功流程
        try (MockedStatic<SpringUtils> springMock = Mockito.mockStatic(SpringUtils.class)) {
            springMock.when(() -> SpringUtils.getBean(eq("aliSmsMsgStrategyImpl"), eq(MsgStrategy.class)))
                    .thenReturn(mockStrategy);

            boolean result = context.execSend(msg, template, List.of(recipient), defInterface, Map.of("debug", true));
            assertTrue(result);
            verify(logManager).incrSuccessCount(555L);
            verify(msgManager).updateById(msg);
            verify(loggingManager).save(any());
        }

        // 2. 测试失败流程
        when(mockStrategy.isSuccess(any())).thenReturn(false);
        try (MockedStatic<SpringUtils> springMock = Mockito.mockStatic(SpringUtils.class)) {
            springMock.when(() -> SpringUtils.getBean(eq("aliSmsMsgStrategyImpl"), eq(MsgStrategy.class)))
                    .thenReturn(mockStrategy);

            boolean result = context.execSend(msg, template, List.of(recipient), defInterface, Map.of());
            assertTrue(result);
            verify(logManager).incrFailCount(555L);
        }

        // 3. 测试异常流程
        when(mockStrategy.exec(any())).thenThrow(new RuntimeException("网络错误"));
        try (MockedStatic<SpringUtils> springMock = Mockito.mockStatic(SpringUtils.class)) {
            springMock.when(() -> SpringUtils.getBean(eq("aliSmsMsgStrategyImpl"), eq(MsgStrategy.class)))
                    .thenReturn(mockStrategy);

            boolean result = context.execSend(msg, template, List.of(recipient), defInterface, Map.of());
            assertTrue(result);
            verify(logManager, times(2)).incrFailCount(555L);
        }
    }

    @Test
    @DisplayName("测试 MsgBiz execSend, sendByTemplate 与 publish 完整校验与分发")
    void testMsgBiz() {
        ExtendMsgService extendMsgService = mock(ExtendMsgService.class);
        DefMsgTemplateService extendMsgTemplateService = mock(DefMsgTemplateService.class);
        ExtendMsgRecipientService extendMsgRecipientService = mock(ExtendMsgRecipientService.class);
        DefInterfaceService defInterfaceService = mock(DefInterfaceService.class);
        DefInterfacePropertyService defInterfacePropertyService = mock(DefInterfacePropertyService.class);
        MsgContext msgContext = mock(MsgContext.class);

        MsgBiz biz = new MsgBiz(extendMsgService, extendMsgTemplateService, extendMsgRecipientService,
                defInterfaceService, defInterfacePropertyService, msgContext);

        // 1. execSend
        ExtendMsg msg = new ExtendMsg();
        msg.setId(10L);
        msg.setTemplateCode("T_001");
        when(extendMsgService.getById(10L)).thenReturn(msg);

        DefMsgTemplate template = new DefMsgTemplate();
        template.setCode("T_001");
        template.setInterfaceId(20L);
        when(extendMsgTemplateService.getByCode("T_001")).thenReturn(template);

        DefInterface defInterface = new DefInterface();
        defInterface.setId(20L);
        when(defInterfaceService.getById(20L)).thenReturn(defInterface);
        when(defInterfacePropertyService.listByInterfaceId(20L)).thenReturn(Map.of("key", "val"));
        when(extendMsgRecipientService.listByMsgId(10L)).thenReturn(List.of(new ExtendMsgRecipient()));
        when(msgContext.execSend(any(), any(), any(), any(), any())).thenReturn(true);

        assertTrue(biz.execSend(10L));

        // 2. sendByTemplate 参数校验
        ExtendMsgSendVO sendVO = new ExtendMsgSendVO();
        assertThrows(ArgumentException.class, () -> biz.sendByTemplate(sendVO, null));

        sendVO.setCode("T_001");
        // 接收人为空抛出异常
        assertThrows(ArgumentException.class, () -> biz.sendByTemplate(sendVO, null));

        sendVO.addRecipient("13900000000");

        // 定时时间不合规 (小于当前时间+5分钟)
        sendVO.setSendTime(LocalDateTime.now().plusMinutes(1));
        assertThrows(ArgumentException.class, () -> biz.sendByTemplate(sendVO, null));

        // 合规定时时间
        sendVO.setSendTime(LocalDateTime.now().plusHours(1));
        when(extendMsgService.send(any(), any(), any())).thenReturn(true);
        assertTrue(biz.sendByTemplate(sendVO, new SysUser()));

        // 3. publish 校验与调用
        ExtendMsgPublishVO publishVO = new ExtendMsgPublishVO();
        assertThrows(ArgumentException.class, () -> biz.publish(publishVO, null));

        publishVO.setRecipientList(List.of("user1"));
        publishVO.setTitle("系统维护");
        publishVO.setContent("今晚停机维护");
        publishVO.setSendTime(LocalDateTime.now().plusDays(1));
        when(extendMsgService.publish(any(), any())).thenReturn(true);
        assertTrue(biz.publish(publishVO, new SysUser()));
    }

    @Test
    @DisplayName("测试 WebSocketObserver 与 WebSocketSubject 发布订阅")
    void testWebSocketObserverAndSubject() throws Exception {
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("sess-001");
        when(mockSession.isOpen()).thenReturn(true);

        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(mockSession.getBasicRemote()).thenReturn(basicRemote);

        WebSocketObserver observer1 = new WebSocketObserver(mockSession);
        WebSocketObserver observer2 = new WebSocketObserver(mockSession);
        assertEquals(observer1, observer2);
        assertEquals(observer1.hashCode(), observer2.hashCode());
        assertEquals(mockSession, observer1.getSession());

        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(1001L);
        assertEquals("1001", subject.getPrincipal());
        subject.addObserver(observer1);

        subject.notify("1", "hello");
        verify(basicRemote).sendText(anyString());

        assertNotNull(WebSocketSubject.Holder.getSubject());
        assertThrows(ArgumentException.class, () -> WebSocketSubject.Holder.getSubject(null));
    }
}
