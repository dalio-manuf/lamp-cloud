package com.dalio.cloud.system.manager.system.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperCacheManagerImpl;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.common.cache.tenant.system.DefClientCacheKeyBuilder;
import com.dalio.cloud.common.cache.tenant.system.DefClientSecretCacheKeyBuilder;
import com.dalio.cloud.system.entity.system.DefClient;
import com.dalio.cloud.system.manager.system.DefClientManager;
import com.dalio.cloud.system.mapper.system.DefClientMapper;

/**
 * <p>
 * 通用业务实现类
 * 客户端
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 * @create [2021-10-13] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefClientManagerImpl extends SuperCacheManagerImpl<DefClientMapper, DefClient> implements DefClientManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new DefClientCacheKeyBuilder();
    }

    @Override
    public DefClient getClient(String clientId, String clientSecret) {
        CacheKey key = DefClientSecretCacheKeyBuilder.builder(clientId, clientSecret);
        CacheResult<Long> result = cacheOps.get(key, k -> {
            DefClient one = getOne(Wraps.<DefClient>lbQ().eq(DefClient::getClientId, clientId).eq(DefClient::getClientSecret, clientSecret));
            return one == null ? null : one.getId();
        });
        Long id = result.asLong();
        ArgumentAssert.notNull(id, "客户端[{}]不存在", clientId);
        return getByIdCache(id);
    }
}
