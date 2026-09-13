package com.dalio.cloud.oauth.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.facade.CaptchaFacade;
import com.dalio.cloud.oauth.service.CaptchaService;

/**
 *
 * @author admin
 * @since 2024/9/20 15:42
 */
@Service
@RequiredArgsConstructor
public class CaptchaFacadeImpl implements CaptchaFacade {

    private final CaptchaService captchaService;

    public Boolean check(String key, String code, String templateCode) {
        R<Boolean> result = captchaService.checkCaptcha(key, code, templateCode);
        return result.getData();
    }
}
