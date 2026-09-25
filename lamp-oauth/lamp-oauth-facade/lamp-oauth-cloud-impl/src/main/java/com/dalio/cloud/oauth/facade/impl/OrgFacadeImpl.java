package com.dalio.cloud.oauth.facade.impl;

import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.oauth.api.OrgApi;
import com.dalio.cloud.oauth.facade.OrgFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 实现
 *
 * @author admin
 * @since 2024/9/20 23:29
 */
@Service(EchoApi.ORG_ID_CLASS)
public class OrgFacadeImpl implements OrgFacade {
    // 一定要延迟加载，否则lamp-gateway-server无法启动
    private final OrgApi orgApi;

    public OrgFacadeImpl(@Lazy OrgApi orgApi) {
        this.orgApi = orgApi;
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return orgApi.findByIds(ids);
    }
}
