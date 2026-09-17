package com.dalio.cloud.msg.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.cloud.job.facade.JobFacade;
import com.dalio.cloud.model.entity.base.SysEmployee;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.enumeration.TaskStatus;
import com.dalio.cloud.msg.manager.ExtendMsgManager;
import com.dalio.cloud.msg.manager.ExtendMsgRecipientManager;
import com.dalio.cloud.msg.manager.ExtendNoticeManager;
import com.dalio.cloud.msg.vo.result.ExtendMsgResultVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgPublishVO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ExtendMsgServiceTest {

    @Test
    @DisplayName("测试 getResultById 正常查询与组装接收人")
    void testGetResultById() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(100L);
        msg.setTitle("系统通知");

        ExtendMsgRecipient r1 = new ExtendMsgRecipient();
        r1.setRecipient("user1");
        ExtendMsgRecipient r2 = new ExtendMsgRecipient();
        r2.setRecipient("user2");

        when(msgManager.getById(100L)).thenReturn(msg);
        when(recipientManager.listByMsgId(100L)).thenReturn(List.of(r1, r2));

        ExtendMsgResultVO result = service.getResultById(100L);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(2, result.getRecipientList().size());
        assertTrue(result.getRecipientList().contains("user1"));
    }

    @Test
    @DisplayName("测试 publish 参数校验防护")
    void testPublishValidation() {
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);

        // 1. data 为 null
        assertThrows(RuntimeException.class, () -> service.publish(null, null));

        // 2. recipientList 为空
        ExtendMsgPublishVO vo = new ExtendMsgPublishVO();
        vo.setTitle("Test");
        vo.setRecipientList(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> service.publish(vo, null));
    }

    @Test
    @DisplayName("测试 publish 立即发布与草稿发布")
    void testPublishImmediateAndDraft() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        when(msgManager.save(any(ExtendMsg.class))).thenAnswer(invocation -> {
            ExtendMsg m = invocation.getArgument(0);
            m.setId(200L);
            return true;
        });

        SysUser user = new SysUser();
        SysEmployee emp = new SysEmployee();
        emp.setLastDeptId(888L);
        user.setEmployee(emp);

        // 1. 立即发布 (草稿为 false, sendTime 为 null)
        ExtendMsgPublishVO vo = new ExtendMsgPublishVO();
        vo.setTitle("即时通知");
        vo.setDraft(false);
        vo.setRecipientList(List.of("1001"));

        Boolean success = service.publish(vo, user);
        assertTrue(success);
        verify(msgManager, atLeastOnce()).save(any(ExtendMsg.class));
        verify(noticeManager).saveBatch(anyList());

        // 2. 草稿保存
        ExtendMsgPublishVO draftVO = new ExtendMsgPublishVO();
        draftVO.setTitle("草稿通知");
        draftVO.setDraft(true);
        draftVO.setRecipientList(List.of("1002"));

        Boolean draftSuccess = service.publish(draftVO, user);
        assertTrue(draftSuccess);
    }

    @Test
    @DisplayName("测试 publish 定时发送任务添加")
    void testPublishScheduled() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        when(msgManager.save(any(ExtendMsg.class))).thenAnswer(invocation -> {
            ExtendMsg m = invocation.getArgument(0);
            m.setId(300L);
            return true;
        });

        ExtendMsgPublishVO vo = new ExtendMsgPublishVO();
        vo.setTitle("定时通知");
        vo.setDraft(false);
        vo.setRecipientList(List.of("1001"));
        vo.setSendTime(LocalDateTime.now().plusDays(1));

        Boolean success = service.publish(vo, null);
        assertTrue(success);
        verify(jobFacade).addTimingTask(any());
    }

    @Test
    @DisplayName("测试 publishNotice 异常防御")
    void testPublishNoticeErrors() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        // 1. 消息不存在
        when(msgManager.getById(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.publishNotice(999L));

        // 2. 接收人列表为空
        ExtendMsg msg = new ExtendMsg();
        msg.setId(999L);
        when(msgManager.getById(999L)).thenReturn(msg);
        when(recipientManager.listByMsgId(999L)).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> service.publishNotice(999L));
    }

    @Test
    @DisplayName("测试 publishNotice 正常发送与站内信生成")
    void testPublishNoticeSuccess() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(10L);
        when(msgManager.getById(10L)).thenReturn(msg);

        ExtendMsgRecipient r1 = new ExtendMsgRecipient();
        r1.setRecipient("2001");
        when(recipientManager.listByMsgId(10L)).thenReturn(List.of(r1));

        service.publishNotice(10L);
        verify(noticeManager).saveBatch(anyList());
        verify(msgManager).updateById(msg);
    }

    @Test
    @DisplayName("测试 send 立即发送与定时发送")
    void testSend() {
        ExtendMsgManager msgManager = mock(ExtendMsgManager.class);
        ExtendMsgRecipientManager recipientManager = mock(ExtendMsgRecipientManager.class);
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        JobFacade jobFacade = mock(JobFacade.class);

        ExtendMsgServiceImpl service = new ExtendMsgServiceImpl(recipientManager, noticeManager, jobFacade);
        ReflectionTestUtils.setField(service, "superManager", msgManager);

        when(msgManager.save(any(ExtendMsg.class))).thenAnswer(invocation -> {
            ExtendMsg m = invocation.getArgument(0);
            m.setId(500L);
            return true;
        });

        com.dalio.cloud.msg.entity.DefMsgTemplate template = new com.dalio.cloud.msg.entity.DefMsgTemplate();
        template.setType("SMS");
        template.setRemindMode("01");

        com.dalio.cloud.msg.vo.update.ExtendMsgSendVO sendVO = new com.dalio.cloud.msg.vo.update.ExtendMsgSendVO();
        sendVO.setCode("SMS_CODE");
        sendVO.setBizType("SMS");
        sendVO.addRecipient("13800000000");

        // 1. 立即发送
        try (var springMock = Mockito.mockStatic(com.dalio.basic.utils.SpringUtils.class)) {
            Boolean res = service.send(sendVO, template, null);
            assertTrue(res);
            springMock.verify(() -> com.dalio.basic.utils.SpringUtils.publishEvent(any()));
        }

        // 2. 定时发送
        sendVO.setSendTime(LocalDateTime.now().plusDays(2));
        Boolean resScheduled = service.send(sendVO, template, null);
        assertTrue(resScheduled);
        verify(jobFacade).addTimingTask(any());
    }
}
