package com.dalio.cloud.system.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.system.api.DefUserApi;
import com.dalio.cloud.system.facade.DefUserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author admin
 * @since 2024/9/20 23:33
 */
@Service(EchoApi.DEF_USER_ID_CLASS)
@RequiredArgsConstructor
public class DefUserFacadeImpl implements DefUserFacade {
    @Autowired
    @Lazy  // 一定要延迟加载，否则lamp-gateway-server无法启动
    private DefUserApi defUserApi;

    @Override
    public R<List<Long>> findAllUserId() {
        return defUserApi.findAllUserId();
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return defUserApi.findByIds(ids);
    }

}

