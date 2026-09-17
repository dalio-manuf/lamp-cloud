package com.dalio.cloud.msg.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 单体版 MsgFacadeImpl 测试
 *
 * @author went
 */
@ExtendWith(MockitoExtension.class)
class MsgFacadeImplTest {

    @Mock
    private MsgBiz msgBiz;

    @InjectMocks
    private MsgFacadeImpl msgFacade;

    @Test
    @DisplayName("测试单体版 sendByTemplate 接口")
    void testSendByTemplate() {
        ExtendMsgSendVO vo = new ExtendMsgSendVO();
        vo.setCode("TMPL_01");

        when(msgBiz.sendByTemplate(eq(vo), isNull())).thenReturn(true);

        Boolean result = msgFacade.sendByTemplate(vo);

        assertTrue(result);
        verify(msgBiz).sendByTemplate(eq(vo), isNull());
    }
}
