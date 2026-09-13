package com.dalio.cloud.system.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.common.cache.tenant.application.AllResourceApiCacheKeyBuilder;
import com.dalio.cloud.system.facade.DefResourceFacade;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 微服务版系统资源权限门面实现
 *
 * @author dalio
 * @since 2024/9/21 22:21
 */
@Service
@RequiredArgsConstructor
public class DefResourceFacadeImpl implements DefResourceFacade {
    private final CacheOps cacheOps;

    @Override
    public Map<String, Set<String>> listAllApi() {
        CacheKey cacheKey = AllResourceApiCacheKeyBuilder.builder();
        CacheResult<Map<String, Set<String>>> result = cacheOps.get(cacheKey);
        return (result != null && result.getValue() != null) ? result.getValue() : Collections.emptyMap();
    }
}
