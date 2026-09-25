package com.dalio.cloud.msg.strategy;

import com.baidubce.services.sms.SmsClient;
import com.baidubce.services.sms.model.SendMessageV3Request;
import com.baidubce.services.sms.model.SendMessageV3Response;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;
import com.dalio.cloud.msg.strategy.impl.sms.BaiduSmsMsgStrategyImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BaiduSmsMsgStrategyImplTest {

    @Test
    @DisplayName("测试 BaiduSmsMsgStrategyImpl debug模式")
    void testExecDebug() {
        BaiduSmsMsgStrategyImpl strategy = new BaiduSmsMsgStrategyImpl();

        Map<String, Object> props = new HashMap<>();
        props.put("debug", true);
        props.put("accessKeyId", "ak");
        props.put("secretKey", "sk");
        props.put("endPoint", "sms.bj.baidubce.com");

        MsgParam param = MsgParam.builder()
                .extendMsg(new ExtendMsg())
                .extendMsgTemplate(new DefMsgTemplate())
                .propertyParams(props)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));
    }

    @Test
    @DisplayName("测试 BaiduSmsMsgStrategyImpl 生产模式模拟发送与 isSuccess 状态判断")
    void testExecProduction() {
        BaiduSmsMsgStrategyImpl strategy = new BaiduSmsMsgStrategyImpl();

        Map<String, Object> props = new HashMap<>();
        props.put("debug", false);
        props.put("accessKeyId", "test-ak");
        props.put("secretKey", "test-sk");
        props.put("endPoint", "sms.bj.baidubce.com");

        ExtendMsg msg = new ExtendMsg();
        msg.setId(101L);
        msg.setParam("[{\"key\":\"code\",\"value\":\"1234\"}]");

        DefMsgTemplate tpl = new DefMsgTemplate();
        tpl.setSign("百度签名");
        tpl.setTemplateCode("SMS_BD_001");

        ExtendMsgRecipient r1 = new ExtendMsgRecipient();
        r1.setRecipient("13800138001");
        ExtendMsgRecipient r2 = new ExtendMsgRecipient();
        r2.setRecipient("13800138002");

        MsgParam param = MsgParam.builder()
                .extendMsg(msg)
                .extendMsgTemplate(tpl)
                .recipientList(List.of(r1, r2))
                .propertyParams(props)
                .build();

        SendMessageV3Response mockResponse = new SendMessageV3Response();
        mockResponse.setCode("1000");
        mockResponse.setMessage("成功");

        try (MockedConstruction<SmsClient> mocked = Mockito.mockConstruction(SmsClient.class,
                (smsClient, context) -> when(smsClient.sendMessage(any(SendMessageV3Request.class))).thenReturn(mockResponse))) {
            MsgResult result = strategy.exec(param);
            assertNotNull(result);
            assertTrue(strategy.isSuccess(result));
        }

        // isSuccess 各种分支覆盖
        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("NOT_RESPONSE").build()));

        SendMessageV3Response failResponse = new SendMessageV3Response();
        failResponse.setCode("500");
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResponse).build()));
    }
}
