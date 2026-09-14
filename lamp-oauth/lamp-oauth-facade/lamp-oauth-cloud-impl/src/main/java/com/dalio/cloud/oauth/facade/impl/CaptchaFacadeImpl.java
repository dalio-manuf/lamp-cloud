package com.dalio.cloud.oauth.facade.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.api.CaptchaApi;
import com.dalio.cloud.oauth.facade.CaptchaFacade;

/**
 *
 * @author admin
 * @since 2024/9/20 15:42
 */
@Service
public class CaptchaFacadeImpl implements CaptchaFacade {
    // 一定要延迟加载，否则lamp-gateway-server无法启动
    private final CaptchaApi captchaApi;

    public CaptchaFacadeImpl(@Lazy CaptchaApi captchaApi) {
        this.captchaApi = captchaApi;
    }

    @Override
    public Boolean check(String key, String code, String templateCode) {
        R<Boolean> check = captchaApi.check(key, code, templateCode);
        return check != null && check.getIsSuccess() && Boolean.TRUE.equals(check.getData());
    }
}
