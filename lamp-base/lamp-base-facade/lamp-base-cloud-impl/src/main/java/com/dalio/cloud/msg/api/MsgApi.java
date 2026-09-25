package com.dalio.cloud.msg.api;


import com.dalio.basic.base.R;
import com.dalio.basic.constant.Constants;
import com.dalio.cloud.msg.api.fallback.MsgApiFallback;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息接口
 *
 * @author admin
 * @date 2019/06/21
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:lamp-base-server}", fallback = MsgApiFallback.class)
public interface MsgApi {

    /**
     * 根据模板发送消息
     *
     * @param data 发送内容
     * @return
     */
    @Operation(summary = "根据模板发送消息", description = "根据模板发送消息")
    @PostMapping("/anyUser/extendMsg/sendByTemplate")
    R<Boolean> sendByTemplate(@RequestBody ExtendMsgSendVO data);
}
