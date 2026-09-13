package com.dalio.cloud.gateway.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 网关端资源权限门面实现
 * 直接从 Redis 缓存获取全量 API 接口与权限标识映射，避免网关依赖数据库与系统业务模块
 *
 * @author dalio
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayResourceFacadeImpl implements DefResourceFacade {
    private final CacheOps cacheOps;

    @Override
    public Map<String, Set<String>> listAllApi() {
        CacheKey cacheKey = AllResourceApiCacheKeyBuilder.builder();
        CacheResult<Map<String, Set<String>>> result = cacheOps.get(cacheKey);
        if (result != null && result.getValue() != null) {
            return result.getValue();
        }
        log.debug("Redis 中尚未加载 API 权限缓存，返回空映射");
        return Collections.emptyMap();
    }
}
