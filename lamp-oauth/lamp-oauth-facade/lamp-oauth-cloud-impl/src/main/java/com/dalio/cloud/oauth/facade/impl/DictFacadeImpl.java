package com.dalio.cloud.oauth.facade.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.oauth.api.DictApi;
import com.dalio.cloud.oauth.facade.DictFacade;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 字典实现
 *
 * @author admin
 * @since 2024/9/20 23:29
 */
@Service(EchoApi.DICTIONARY_ITEM_FEIGN_CLASS)
public class DictFacadeImpl implements DictFacade {
    // 一定要延迟加载，否则lamp-gateway-server无法启动
    private final DictApi dictApi;

    public DictFacadeImpl(@Lazy DictApi dictApi) {
        this.dictApi = dictApi;
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return dictApi.findByIds(ids);
    }
}
