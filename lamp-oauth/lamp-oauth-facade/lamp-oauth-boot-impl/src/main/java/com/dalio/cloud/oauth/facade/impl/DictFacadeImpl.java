package com.dalio.cloud.oauth.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.oauth.facade.DictFacade;
import com.dalio.cloud.oauth.service.DictService;

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
@RequiredArgsConstructor
public class DictFacadeImpl implements DictFacade {
    private final DictService dictService;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return dictService.findByIds(ids);
    }
}
