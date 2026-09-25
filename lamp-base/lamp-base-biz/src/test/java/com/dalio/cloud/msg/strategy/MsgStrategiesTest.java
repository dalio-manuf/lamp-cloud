package com.dalio.cloud.msg.strategy;

import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.manager.ExtendNoticeManager;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;
import com.dalio.cloud.msg.strategy.impl.NoticeMsgStrategyImpl;
import com.dalio.cloud.msg.strategy.impl.mail.TencentMailMsgStrategyImpl;
import com.dalio.cloud.msg.strategy.impl.sms.AliSmsMsgStrategyImpl;
import com.dalio.cloud.msg.strategy.impl.sms.ClSmsMsgStrategyImpl;
import com.dalio.cloud.msg.strategy.impl.sms.TencentSmsMsgStrategyImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 消息发送策略单元测试
 */
class MsgStrategiesTest {

    @Test
    @DisplayName("测试 AliSmsMsgStrategyImpl debug模式与isSuccess判断")
    void testAliSmsMsgStrategy() throws Exception {
        AliSmsMsgStrategyImpl strategy = new AliSmsMsgStrategyImpl();

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(10L);
        extendMsg.setParam("{\"code\":\"123456\"}");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setSign("测试签名");
        template.setTemplateCode("SMS_123456");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13800138000");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", true);
        props.put("accessKeyId", "ak");
        props.put("accessKeySecret", "sk");
        props.put("endpoint", "dysmsapi.aliyuncs.com");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .propertyParams(props)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));
    }

    @Test
    @DisplayName("测试 ClSmsMsgStrategyImpl debug模式与isSuccess判断")
    void testClSmsMsgStrategy() throws Exception {
        ClSmsMsgStrategyImpl strategy = new ClSmsMsgStrategyImpl();

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(20L);
        extendMsg.setContent("您的验证码是${code}");
        extendMsg.setParam("[{\"key\":\"code\",\"value\":\"8888\"}]");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setSign("创蓝测试");
        template.setContent("您的验证码是${code}");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13900139000");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", true);
        props.put("account", "test_acc");
        props.put("password", "test_pwd");
        props.put("endPoint", "http://sms.253.com/msg/send/json");
        props.put("variable", true);

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .propertyParams(props)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));
    }

    @Test
    @DisplayName("测试 TencentSmsMsgStrategyImpl debug模式与isSuccess判断")
    void testTencentSmsMsgStrategy() throws Exception {
        TencentSmsMsgStrategyImpl strategy = new TencentSmsMsgStrategyImpl();

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(30L);
        extendMsg.setParam("{\"code\":\"6666\"}");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setSign("腾讯云");
        template.setTemplateCode("12345");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("+8613700137000");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", true);
        props.put("sdkAppId", "1400000000");
        props.put("secretId", "sid");
        props.put("secretKey", "skey");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .propertyParams(props)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));
    }

    @Test
    @DisplayName("测试 TencentMailMsgStrategyImpl debug模式")
    void testTencentMailMsgStrategy() {
        TencentMailMsgStrategyImpl strategy = new TencentMailMsgStrategyImpl();

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(40L);
        extendMsg.setTitle("欢迎加入");
        extendMsg.setContent("请点击链接激活账号");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setTitle("欢迎加入");
        template.setContent("请点击链接激活账号");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("test@example.com");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", true);
        props.put("hostName", "smtp.exmail.qq.com");
        props.put("username", "admin@example.com");
        props.put("password", "pwd");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .propertyParams(props)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertEquals("debug模式无需发送", result.getResult());
    }

    @Test
    @DisplayName("测试 NoticeMsgStrategyImpl 站内信发送与保存")
    void testNoticeMsgStrategy() {
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        NoticeMsgStrategyImpl strategy = new NoticeMsgStrategyImpl(noticeManager);

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(50L);
        extendMsg.setTitle("会议通知");
        extendMsg.setContent("下午2点开会");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setTitle("会议通知");
        template.setContent("下午2点开会");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("1001");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));
        verify(noticeManager).saveBatch(anyList());
    }

    @Test
    @DisplayName("测试 AliSmsMsgStrategyImpl 生产模式与 isSuccess 各种分支")
    void testAliSmsMsgStrategyProduction() throws Exception {
        AliSmsMsgStrategyImpl strategy = new AliSmsMsgStrategyImpl();

        com.aliyun.dysmsapi20170525.models.SendSmsResponse okResponse = new com.aliyun.dysmsapi20170525.models.SendSmsResponse();
        com.aliyun.dysmsapi20170525.models.SendSmsResponseBody body = new com.aliyun.dysmsapi20170525.models.SendSmsResponseBody();
        body.setCode("OK");
        okResponse.setBody(body);

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(11L);
        extendMsg.setParam("[{\"key\":\"code\",\"value\":\"1234\"}]");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setSign("阿里测试");
        template.setTemplateCode("SMS_9999");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13800138000");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", false);
        props.put("accessKeyId", "ali-ak");
        props.put("accessKeySecret", "ali-sk");
        props.put("regionId", "cn-hangzhou");
        props.put("endpoint", "dysmsapi.aliyuncs.com");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(recipient))
                .propertyParams(props)
                .build();

        try (org.mockito.MockedConstruction<com.aliyun.dysmsapi20170525.Client> mocked = Mockito.mockConstruction(
                com.aliyun.dysmsapi20170525.Client.class,
                (client, context) -> when(client.sendSms(any())).thenReturn(okResponse))) {

            MsgResult result = strategy.exec(param);
            assertNotNull(result);
            assertTrue(strategy.isSuccess(result));
        }

        // isSuccess 分支测试
        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("STRING").build()));

        com.aliyun.dysmsapi20170525.models.SendSmsResponse failResponse = new com.aliyun.dysmsapi20170525.models.SendSmsResponse();
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResponse).build()));

        com.aliyun.dysmsapi20170525.models.SendSmsResponseBody failBody = new com.aliyun.dysmsapi20170525.models.SendSmsResponseBody();
        failBody.setCode("FAIL");
        failResponse.setBody(failBody);
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResponse).build()));
    }

    @Test
    @DisplayName("测试 TencentSmsMsgStrategyImpl isSuccess 各种分支")
    void testTencentSmsMsgStrategyIsSuccess() {
        TencentSmsMsgStrategyImpl strategy = new TencentSmsMsgStrategyImpl();

        com.tencentcloudapi.sms.v20190711.models.SendSmsResponse okResponse = new com.tencentcloudapi.sms.v20190711.models.SendSmsResponse();
        com.tencentcloudapi.sms.v20190711.models.SendStatus okStatus = new com.tencentcloudapi.sms.v20190711.models.SendStatus();
        okStatus.setCode("Ok");
        okResponse.setSendStatusSet(new com.tencentcloudapi.sms.v20190711.models.SendStatus[]{okStatus});

        assertTrue(strategy.isSuccess(MsgResult.builder().result(okResponse).build()));
        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("STRING").build()));

        com.tencentcloudapi.sms.v20190711.models.SendSmsResponse emptyResponse = new com.tencentcloudapi.sms.v20190711.models.SendSmsResponse();
        assertFalse(strategy.isSuccess(MsgResult.builder().result(emptyResponse).build()));

        com.tencentcloudapi.sms.v20190711.models.SendSmsResponse failResponse = new com.tencentcloudapi.sms.v20190711.models.SendSmsResponse();
        com.tencentcloudapi.sms.v20190711.models.SendStatus failStatus = new com.tencentcloudapi.sms.v20190711.models.SendStatus();
        failStatus.setCode("Fail");
        failResponse.setSendStatusSet(new com.tencentcloudapi.sms.v20190711.models.SendStatus[]{failStatus});
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResponse).build()));
    }

    @Test
    @DisplayName("测试 ClSmsMsgStrategyImpl 变量与短信拼接方法及 isSuccess 分支")
    void testClSmsMsgStrategyHelpers() {
        ClSmsMsgStrategyImpl strategy = new ClSmsMsgStrategyImpl();

        String msg1 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(strategy, "buildMsg", "创蓝", "验证码${code},有效期${time}");
        assertEquals("【创蓝】验证码{$var},有效期{$var}", msg1);

        String msg2 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(strategy, "buildVariableMsg", "创蓝", "普通内容无需替换");
        assertEquals("【创蓝】普通内容无需替换", msg2);

        String emptyMsg = org.springframework.test.util.ReflectionTestUtils.invokeMethod(strategy, "replaceMsg", "");
        assertEquals("", emptyMsg);

        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("FAIL").build()));

        com.dalio.cloud.msg.strategy.domain.sms.ClSendResult failResult = new com.dalio.cloud.msg.strategy.domain.sms.ClSendResult();
        failResult.setCode("-1");
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResult).build()));
    }

    @Test
    @DisplayName("测试 TencentMailMsgStrategyImpl 生产模式构造与发送邮件")
    void testTencentMailMsgStrategyProduction() {
        TencentMailMsgStrategyImpl strategy = new TencentMailMsgStrategyImpl();

        ExtendMsg extendMsg = new ExtendMsg();
        extendMsg.setId(41L);
        extendMsg.setTitle("测试邮件标题");
        extendMsg.setContent("测试邮件内容");
        extendMsg.setParam("[{\"key\":\"title\",\"value\":\"测试\"},{\"key\":\"content\",\"value\":\"详情\"}]");

        DefMsgTemplate template = new DefMsgTemplate();
        template.setTitle("欢迎加入${title}");
        template.setContent("内容：${content}");

        ExtendMsgRecipient r1 = new ExtendMsgRecipient();
        r1.setRecipient("u1@example.com");
        r1.setExt("张三");

        ExtendMsgRecipient r2 = new ExtendMsgRecipient();
        r2.setRecipient("u2@example.com");

        Map<String, Object> props = new HashMap<>();
        props.put("debug", false);
        props.put("hostName", "smtp.exmail.qq.com");
        props.put("username", "admin@example.com");
        props.put("password", "pwd123");
        props.put("ssl", true);
        props.put("smtpPort", "465");
        props.put("charset", "UTF-8");
        props.put("fromName", "管理员");
        props.put("fromEmail", "admin@example.com");

        MsgParam param = MsgParam.builder()
                .extendMsg(extendMsg)
                .extendMsgTemplate(template)
                .recipientList(List.of(r1, r2))
                .propertyParams(props)
                .build();

        try (org.mockito.MockedConstruction<org.apache.commons.mail.HtmlEmail> mocked = Mockito.mockConstruction(
                org.apache.commons.mail.HtmlEmail.class,
                (email, context) -> when(email.send()).thenReturn("msg-id-12345"))) {

            MsgResult result = strategy.exec(param);
            assertNotNull(result);
            assertEquals("msg-id-12345", result.getResult());
        }

        // 测试非 SSL 端口配置
        props.put("ssl", false);
        props.put("smtpPort", "25");
        try (org.mockito.MockedConstruction<org.apache.commons.mail.HtmlEmail> mocked = Mockito.mockConstruction(
                org.apache.commons.mail.HtmlEmail.class,
                (email, context) -> when(email.send()).thenReturn("msg-id-25"))) {

            MsgResult result = strategy.exec(param);
            assertNotNull(result);
            assertEquals("msg-id-25", result.getResult());
        }
    }

    @Test
    @DisplayName("测试 MsgStrategy 接口默认方法 (parseParam 与 replaceVariable)")
    void testMsgStrategyDefaultMethods() {
        MsgStrategy dummyStrategy = param -> MsgResult.builder().build();

        assertTrue(dummyStrategy.isSuccess(MsgResult.builder().build()));

        // 1. parseParam
        assertTrue(dummyStrategy.parseParam(null).isEmpty());
        assertTrue(dummyStrategy.parseParam("").isEmpty());
        Map<String, String> parsed = dummyStrategy.parseParam("[{\"key\":\"k1\",\"value\":\"v1\"},{\"key\":\"k2\",\"value\":\"v2\"}]");
        assertEquals(2, parsed.size());
        assertEquals("v1", parsed.get("k1"));
        assertEquals("v2", parsed.get("k2"));

        // 2. replaceVariable
        ExtendMsg msg = new ExtendMsg();
        msg.setParam("[{\"key\":\"name\",\"value\":\"张三\"}]");

        DefMsgTemplate tpl = new DefMsgTemplate();
        tpl.setTitle("你好, ${name}");
        tpl.setContent("尊敬的${name}，欢迎使用！");

        MsgResult replaced = dummyStrategy.replaceVariable(msg, tpl);
        assertNotNull(replaced);
        assertTrue(replaced.getTitle().contains("张三"));
        assertTrue(replaced.getContent().contains("张三"));
    }
}
