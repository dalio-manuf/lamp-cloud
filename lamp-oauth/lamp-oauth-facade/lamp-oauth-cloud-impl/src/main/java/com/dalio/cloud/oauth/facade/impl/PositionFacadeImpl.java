package com.dalio.cloud.oauth.facade.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.oauth.api.PositionApi;
import com.dalio.cloud.oauth.facade.PositionFacade;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 实现
 * @author admin
 * @since 2024/9/20 23:29
 */
@Service(EchoApi.POSITION_ID_CLASS)
public class PositionFacadeImpl implements PositionFacade {
    // 一定要延迟加载，否则lamp-gateway-server无法启动
    private final PositionApi positionApi;

    public PositionFacadeImpl(@Lazy PositionApi positionApi) {
        this.positionApi = positionApi;
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return positionApi.findByIds(ids);
    }
}
