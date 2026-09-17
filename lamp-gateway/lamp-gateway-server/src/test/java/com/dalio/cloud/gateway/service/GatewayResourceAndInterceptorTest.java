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

    @Test
    @DisplayName("测试 AuthenticationSaInterceptor 开启鉴权模式下的权限检查与未授权拦截")
    void testAuthenticationWithAuthEnabled() {
        DefResourceFacade facade = Mockito.mock(DefResourceFacade.class);
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        ignoreProperties.setAuthEnabled(true);
        ignoreProperties.setNotConfigUriAllow(false);

        Map<String, Set<String>> apiMap = new java.util.HashMap<>();
        apiMap.put("/api/user/list###GET", Collections.singleton("user:view"));
        when(facade.listAllApi()).thenReturn(apiMap);

        AuthenticationSaInterceptor interceptor = new AuthenticationSaInterceptor(facade, ignoreProperties);

        try (org.mockito.MockedStatic<cn.dev33.satoken.stp.StpUtil> mockedStp = Mockito.mockStatic(cn.dev33.satoken.stp.StpUtil.class)) {
            // 1. 已配置接口，鉴权成功
            MockServerWebExchange exSuccess = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/user/list").build()
            );
            AtomicBoolean chainCalled = new AtomicBoolean(false);
            WebFilterChain chain = ex -> {
                chainCalled.set(true);
                return Mono.empty();
            };

            interceptor.filter(exSuccess, chain).block();
            assertTrue(chainCalled.get());
            mockedStp.verify(() -> cn.dev33.satoken.stp.StpUtil.checkLogin());
            mockedStp.verify(() -> cn.dev33.satoken.stp.StpUtil.checkPermissionOr("user:view"));

            // 2. 未配置接口且 notConfigUriAllow 为 false -> 拦截并返回 JSON 错误
            MockServerWebExchange exUnconfigured = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/unknown/path").build()
            );
            interceptor.filter(exUnconfigured, chain).block();
            assertEquals("application/json", exUnconfigured.getResponse().getHeaders().getFirst(org.springframework.http.HttpHeaders.CONTENT_TYPE));

            // 3. 未登录异常 (SaTokenException) 拦截
            mockedStp.when(cn.dev33.satoken.stp.StpUtil::checkLogin)
                    .thenThrow(new cn.dev33.satoken.exception.NotLoginException("未登录", "login", "token"));
            MockServerWebExchange exNotLogin = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/user/list").build()
            );
            interceptor.filter(exNotLogin, chain).block();
            assertEquals("application/json", exNotLogin.getResponse().getHeaders().getFirst(org.springframework.http.HttpHeaders.CONTENT_TYPE));
        }
    }
}
