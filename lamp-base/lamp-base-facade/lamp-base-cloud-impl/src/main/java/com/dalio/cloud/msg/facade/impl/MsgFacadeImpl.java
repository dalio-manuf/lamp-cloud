package com.dalio.cloud.msg.facade.impl;


import com.dalio.basic.base.R;
import com.dalio.cloud.msg.api.MsgApi;
import com.dalio.cloud.msg.facade.MsgFacade;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 消息接口
 *
 * @author admin
 * @since 2024年09月20日10:37:50
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class MsgFacadeImpl implements MsgFacade {

    private final MsgApi msgApi;

    /**
     * 根据模板发送消息
     *
     * @param data 发送内容
     * @return 是否成功
     */
    @Override
    public Boolean sendByTemplate(ExtendMsgSendVO data) {
        R<Boolean> result = msgApi.sendByTemplate(data);
        return result != null && result.getIsSuccess() && Boolean.TRUE.equals(result.getData());
    }
}
