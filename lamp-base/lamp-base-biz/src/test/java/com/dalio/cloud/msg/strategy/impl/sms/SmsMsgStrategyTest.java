package com.dalio.cloud.msg.strategy.impl.sms;

import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;
import com.dalio.cloud.msg.strategy.domain.sms.ClSendResult;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20190711.models.SendStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClSmsMsgStrategyImpl 与 TencentSmsMsgStrategyImpl 单元测试
 */
class SmsMsgStrategyTest {

    @Test
    @DisplayName("测试 ClSmsMsgStrategyImpl Debug 模式执行与 isSuccess 校验")
    void testClSmsMsgStrategyImpl() throws Exception {
        ClSmsMsgStrategyImpl strategy = new ClSmsMsgStrategyImpl();

        Map<String, Object> propertyParams = new HashMap<>();
        propertyParams.put("account", "test_acc");
        propertyParams.put("password", "test_pwd");
        propertyParams.put("debug", true);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(1L);
        msg.setParam("[{\"key\":\"code\",\"value\":\"1234\"}]");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13800000000");

        MsgParam param = MsgParam.builder()
                .extendMsg(msg)
                .extendMsgTemplate(new DefMsgTemplate())
                .recipientList(List.of(recipient))
                .propertyParams(propertyParams)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));

        // isSuccess 边界分支
        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("string").build()));

        ClSendResult failSend = new ClSendResult();
        failSend.setCode("500");
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failSend).build()));
    }

    @Test
    @DisplayName("测试 TencentSmsMsgStrategyImpl Debug 模式执行与 isSuccess 校验")
    void testTencentSmsMsgStrategyImpl() throws Exception {
        TencentSmsMsgStrategyImpl strategy = new TencentSmsMsgStrategyImpl();

        Map<String, Object> propertyParams = new HashMap<>();
        propertyParams.put("sdkAppId", "1400000000");
        propertyParams.put("secretId", "test_id");
        propertyParams.put("secretKey", "test_key");
        propertyParams.put("debug", true);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(2L);
        msg.setParam("[{\"key\":\"code\",\"value\":\"5678\"}]");

        ExtendMsgRecipient recipient = new ExtendMsgRecipient();
        recipient.setRecipient("13900000000");

        MsgParam param = MsgParam.builder()
                .extendMsg(msg)
                .extendMsgTemplate(new DefMsgTemplate())
                .recipientList(List.of(recipient))
                .propertyParams(propertyParams)
                .build();

        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertTrue(strategy.isSuccess(result));

        // isSuccess 边界分支
        assertFalse(strategy.isSuccess(null));
        assertFalse(strategy.isSuccess(MsgResult.builder().result("invalid").build()));

        SendSmsResponse failResp = new SendSmsResponse();
        SendStatus status = new SendStatus();
        status.setCode("Failed");
        failResp.setSendStatusSet(new SendStatus[]{status});
        assertFalse(strategy.isSuccess(MsgResult.builder().result(failResp).build()));
    }
}
