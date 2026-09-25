package com.dalio.cloud.oauth.facade.impl;

import com.dalio.basic.model.log.OptLogDTO;
import com.dalio.cloud.oauth.api.LogApi;
import com.dalio.cloud.oauth.facade.LogFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 操作日志保存 API
 *
 * @author admin
 * @date 2019/07/02
 */
@Service
public class LogFacadeImpl implements LogFacade {
    // 一定要延迟加载，否则lamp-gateway-server无法启动
    private final LogApi logApi;

    public LogFacadeImpl(@Lazy LogApi logApi) {
        this.logApi = logApi;
    }

    /**
     * 保存日志
     *
     * @param data 操作日志
     */
    @Override
    public void save(OptLogDTO data) {
        logApi.save(data);
    }

}
