/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dalio.cloud.oauth.granter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.dalio.basic.base.R;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.basic.utils.StrHelper;
import com.dalio.cloud.model.enumeration.base.MsgTemplateCodeEnum;
import com.dalio.cloud.oauth.event.LoginEvent;
import com.dalio.cloud.oauth.event.model.LoginStatusDTO;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.vo.param.LoginParamVO;
import com.dalio.cloud.oauth.vo.result.LoginResultVO;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.enumeration.system.LoginStatusEnum;

import static com.dalio.cloud.oauth.granter.MobileTokenGranter.GRANT_TYPE;


/**
 * 手机号登录获取token
 *
 * @author Dave Syer
 * @author admin
 * @date 2020年03月31日10:22:55
 */
@Component(GRANT_TYPE)
@RequiredArgsConstructor
public class MobileTokenGranter extends AbstractTokenGranter {

    public static final String GRANT_TYPE = "MOBILE";
    private final CaptchaService captchaService;

    @Override
    public R<LoginResultVO> checkParam(LoginParamVO loginParam) {
        String mobile = loginParam.getMobile();
        String code = loginParam.getCode();
        if (StrHelper.isAnyBlank(mobile, code)) {
            return R.fail("请输入手机号或验证码");
        }

        return R.success(null);
    }

    @Override
    protected R<LoginResultVO> checkCaptcha(LoginParamVO loginParam) {
        if (Boolean.TRUE.equals(systemProperties.getVerifyCaptcha())) {
            R<Boolean> check = captchaService.checkCaptcha(loginParam.getMobile(), MsgTemplateCodeEnum.MOBILE_LOGIN.getCode(), loginParam.getCode());
            if (check == null || !check.getIsSuccess()) {
                String msg = check != null ? check.getMsg() : "短信验证码错误";
                SpringUtils.publishEvent(new LoginEvent(LoginStatusDTO.smsCodeError(loginParam.getMobile(), LoginStatusEnum.SMS_CODE_ERROR, msg)));
                throw BizException.validFail(msg);
            }
        }
        return R.success(null);
    }

    @Override
    protected DefUser getUser(LoginParamVO loginParam) {
        return defUserService.getUserByMobile(loginParam.getMobile());
    }

}
