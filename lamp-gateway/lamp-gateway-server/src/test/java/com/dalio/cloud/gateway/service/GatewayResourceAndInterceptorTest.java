package com.dalio.cloud.gateway.service;

import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.gateway.filter.AuthenticationSaInterceptor;
import com.dalio.cloud.gateway.filter.OrderedConstant;
import com.dalio.cloud.gateway.service.impl.GatewayResourceFacadeImpl;
import com.dalio.cloud.system.facade.DefResourceFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * GatewayResourceFacadeImpl 与 AuthenticationSaInterceptor 单元测试
 */
class GatewayResourceAndInterceptorTest {

    @Test
    @DisplayName("测试 GatewayResourceFacadeImpl 缓存命中与空缓存回退")
    void testGatewayResourceFacadeImpl() {
        CacheOps cacheOps = Mockito.mock(CacheOps.class);
        GatewayResourceFacadeImpl facade = new GatewayResourceFacadeImpl(cacheOps);

        // 1. 缓存为空
        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class))).thenReturn(null);
        Map<String, Set<String>> emptyMap = facade.listAllApi();
        assertNotNull(emptyMap);
        assertTrue(emptyMap.isEmpty());

        // 2. 缓存命中
        Map<String, Set<String>> cachedData = Collections.singletonMap("/api/demo###GET", Collections.singleton("demo:view"));
        @SuppressWarnings("unchecked")
        CacheResult<Map<String, Set<String>>> cacheResult = Mockito.mock(CacheResult.class);
        when(cacheResult.getValue()).thenReturn(cachedData);
        Mockito.doReturn(cacheResult).when(cacheOps).get(any(com.dalio.basic.model.cache.CacheKey.class));

        Map<String, Set<String>> resultMap = facade.listAllApi();
        assertNotNull(resultMap);
        assertEquals(1, resultMap.size());
        assertTrue(resultMap.containsKey("/api/demo###GET"));
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cn.dev33.satoken.strategy.SaStrategy.instance.routeMatcher = (pattern, path) -> 
                cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil.match(pattern, path);
    }

    @Test
    @DisplayName("测试 AuthenticationSaInterceptor 顺序与无需登录接口放行")
    void testAuthenticationSaInterceptor() {
        DefResourceFacade facade = Mockito.mock(DefResourceFacade.class);
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        AuthenticationSaInterceptor interceptor = new AuthenticationSaInterceptor(facade, ignoreProperties);

        assertEquals(OrderedConstant.AUTHENTICATION, interceptor.getOrder());

        // 白名单接口放行 (/swagger-ui.html)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/swagger-ui.html").build()
        );
        AtomicBoolean called = new AtomicBoolean(false);
        WebFilterChain chain = ex -> {
            called.set(true);
            return Mono.empty();
        };

        interceptor.filter(exchange, chain).block();
        assertTrue(called.get());
    }
}
