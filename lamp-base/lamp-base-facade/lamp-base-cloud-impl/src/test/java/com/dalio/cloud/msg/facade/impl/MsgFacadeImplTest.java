package com.dalio.cloud.msg.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dalio.basic.base.R;
import com.dalio.cloud.msg.api.MsgApi;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微服务版 MsgFacadeImpl 测试
 *
 * @author went
 */
@ExtendWith(MockitoExtension.class)
class MsgFacadeImplTest {

    @Mock
    private MsgApi msgApi;

    @InjectMocks
    private MsgFacadeImpl msgFacade;

    @Test
    @DisplayName("测试 sendByTemplate 成功返回 true")
    void testSendByTemplateSuccess() {
        ExtendMsgSendVO vo = new ExtendMsgSendVO();
        when(msgApi.sendByTemplate(vo)).thenReturn(R.success(true));

        Boolean result = msgFacade.sendByTemplate(vo);

        assertTrue(result);
        verify(msgApi).sendByTemplate(vo);
    }

    @Test
    @DisplayName("测试 sendByTemplate 业务返回 false")
    void testSendByTemplateFalse() {
        ExtendMsgSendVO vo = new ExtendMsgSendVO();
        when(msgApi.sendByTemplate(vo)).thenReturn(R.success(false));

        Boolean result = msgFacade.sendByTemplate(vo);

        assertFalse(result);
    }

    @Test
    @DisplayName("测试 sendByTemplate 失败或空响应返回 false")
    void testSendByTemplateFailure() {
        ExtendMsgSendVO vo = new ExtendMsgSendVO();
        when(msgApi.sendByTemplate(vo)).thenReturn(R.fail("failed"));

        Boolean result = msgFacade.sendByTemplate(vo);
        assertFalse(result);

        when(msgApi.sendByTemplate(vo)).thenReturn(null);
        assertFalse(msgFacade.sendByTemplate(vo));
    }
}
