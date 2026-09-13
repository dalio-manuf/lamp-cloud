package com.dalio.cloud.oauth.granter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.dalio.basic.base.R;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.basic.utils.StrHelper;
import com.dalio.cloud.oauth.event.LoginEvent;
import com.dalio.cloud.oauth.event.model.LoginStatusDTO;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.vo.param.LoginParamVO;
import com.dalio.cloud.oauth.vo.result.LoginResultVO;
import com.dalio.cloud.system.enumeration.system.LoginStatusEnum;

import static com.dalio.cloud.oauth.granter.CaptchaTokenGranter.GRANT_TYPE;

/**
 * 验证码TokenGranter
 *
 * @author admin
 */
@Component(GRANT_TYPE)
@Slf4j
@RequiredArgsConstructor
public class CaptchaTokenGranter extends PasswordTokenGranter implements TokenGranter {

    public static final String GRANT_TYPE = "CAPTCHA";
    private final CaptchaService captchaService;

    @Override
    protected R<LoginResultVO> checkCaptcha(LoginParamVO loginParam) {
        if (systemProperties.getVerifyCaptcha()) {
            R<Boolean> check = captchaService.checkCaptcha(loginParam.getKey(), GRANT_TYPE, loginParam.getCode());
            if (!check.getIsSuccess()) {
                String msg = check.getMsg();
                SpringUtils.publishEvent(new LoginEvent(LoginStatusDTO.fail(loginParam.getUsername(), LoginStatusEnum.CAPTCHA_ERROR, msg)));
                throw BizException.validFail(check.getMsg());
            }
        }
        return R.success(null);
    }

    @Override
    public R<LoginResultVO> checkParam(LoginParamVO loginParam) {
        String username = loginParam.getUsername();
        String password = loginParam.getPassword();
        if (StrHelper.isAnyBlank(username, password)) {
            return R.fail("请输入用户名或密码");
        }
        if (StrHelper.isAnyBlank(loginParam.getCode(), loginParam.getKey())) {
            return R.fail("请输入验证码");
        }

        return R.success(null);
    }

}
