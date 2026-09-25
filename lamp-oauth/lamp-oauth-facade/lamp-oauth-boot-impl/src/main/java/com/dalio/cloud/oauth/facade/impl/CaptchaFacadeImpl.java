package com.dalio.cloud.oauth.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.facade.CaptchaFacade;
import com.dalio.cloud.oauth.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author admin
 * @since 2024/9/20 15:42
 */
@Service
@RequiredArgsConstructor
public class CaptchaFacadeImpl implements CaptchaFacade {

    private final CaptchaService captchaService;

    @Override
    public Boolean check(String key, String code, String templateCode) {
        R<Boolean> result = captchaService.checkCaptcha(key, templateCode, code);
        return result != null && result.getIsSuccess() && Boolean.TRUE.equals(result.getData());
    }
}
