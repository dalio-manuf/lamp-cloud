package com.dalio.cloud.msg.api.fallback;

import com.dalio.basic.base.R;
import com.dalio.cloud.msg.api.MsgApi;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import org.springframework.stereotype.Component;

/**
 * 熔断
 *
 * @author admin
 * @date 2019/07/25
 */
@Component
public class MsgApiFallback implements MsgApi {
    @Override
    public R<Boolean> sendByTemplate(ExtendMsgSendVO data) {
        return R.timeout();
    }
}
