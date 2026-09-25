package com.dalio.cloud.system.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.system.api.DefUserApi;
import com.dalio.cloud.system.api.hystrix.DefUserApiFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 微服务版 DefResourceFacadeImpl、DefUserFacadeImpl 及 DefUserApiFallback 单元测试
 */
class DefFacadeCloudImplTest {

    @Test
    @DisplayName("测试微服务版 DefResourceFacadeImpl listAllApi 缓存命中与空分支")
    void testDefResourceFacadeImpl() {
        CacheOps cacheOps = mock(CacheOps.class);
        DefResourceFacadeImpl facade = new DefResourceFacadeImpl(cacheOps);

        // 1. 缓存未命中或为空
        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class), any(boolean[].class))).thenReturn(null);
        assertTrue(facade.listAllApi().isEmpty());

        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class), any(boolean[].class))).thenReturn(new CacheResult<>("k", null));
        assertTrue(facade.listAllApi().isEmpty());

        // 2. 缓存命中
        Map<String, Set<String>> cachedMap = Map.of("/api/test###GET", Set.of("TEST_VIEW"));
        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class), any(boolean[].class))).thenReturn(new CacheResult<>("k", cachedMap));
        Map<String, Set<String>> result = facade.listAllApi();
        assertEquals(cachedMap, result);
    }

    @Test
    @DisplayName("测试微服务版 DefUserFacadeImpl 远程调用委托")
    void testDefUserFacadeImpl() {
        DefUserApi userApi = mock(DefUserApi.class);
        DefUserFacadeImpl facade = new DefUserFacadeImpl();
        ReflectionTestUtils.setField(facade, "defUserApi", userApi);

        // 1. findAllUserId
        when(userApi.findAllUserId()).thenReturn(R.success(List.of(10L, 20L)));
        R<List<Long>> idsR = facade.findAllUserId();
        assertTrue(idsR.getIsSuccess());
        assertEquals(List.of(10L, 20L), idsR.getData());

        // 2. findByIds
        Set<Serializable> ids = Set.of(10L);
        when(userApi.findByIds(ids)).thenReturn(Map.of(10L, "test_user"));
        Map<Serializable, Object> userMap = facade.findByIds(ids);
        assertEquals(1, userMap.size());
        assertEquals("test_user", userMap.get(10L));
    }

    @Test
    @DisplayName("测试 DefUserApiFallback 熔断降级返回")
    void testDefUserApiFallback() {
        DefUserApiFallback fallback = new DefUserApiFallback();

        R<List<Long>> userIds = fallback.findAllUserId();
        assertNotNull(userIds);
        assertEquals(R.TIMEOUT_CODE, userIds.getCode());

        Map<Serializable, Object> byIds = fallback.findByIds(Set.of(1L));
        assertNotNull(byIds);
        assertTrue(byIds.isEmpty());

        R<SysUser> userR = fallback.getById(new UserQuery());
        assertNotNull(userR);
        assertEquals(R.TIMEOUT_CODE, userR.getCode());
    }
}
