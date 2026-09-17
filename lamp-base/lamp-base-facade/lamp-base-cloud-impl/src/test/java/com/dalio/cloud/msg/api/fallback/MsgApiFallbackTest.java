package com.dalio.cloud.msg.api.fallback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.basic.base.R;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MsgApiFallback 单元测试
 *
 * @author went
 */
class MsgApiFallbackTest {

    @Test
    @DisplayName("测试 MsgApiFallback.sendByTemplate 熔断返回 timeout")
    void testSendByTemplateFallback() {
        MsgApiFallback fallback = new MsgApiFallback();
        ExtendMsgSendVO vo = new ExtendMsgSendVO();

        R<Boolean> result = fallback.sendByTemplate(vo);

        assertNotNull(result);
        assertEquals(R.TIMEOUT_CODE, result.getCode());
    }
}
