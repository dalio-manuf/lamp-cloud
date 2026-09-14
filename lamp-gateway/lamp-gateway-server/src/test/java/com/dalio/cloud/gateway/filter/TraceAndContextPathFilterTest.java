package com.dalio.cloud.gateway.filter;

import com.dalio.basic.context.ContextConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceFilter 与 ContextPathFilter 单元测试
 */
class TraceAndContextPathFilterTest {

    @Test
    @DisplayName("测试 TraceFilter 自动生成与保留已有的 TraceId")
    void testTraceFilter() {
        TraceFilter traceFilter = new TraceFilter();
        assertEquals(OrderedConstant.TRACE, traceFilter.getOrder());

        // 1. 无 TraceId，自动生成
        MockServerWebExchange exchange1 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
        AtomicReference<String> passedTraceId1 = new AtomicReference<>();
        WebFilterChain chain1 = ex -> {
            passedTraceId1.set(ex.getRequest().getHeaders().getFirst(ContextConstants.TRACE_ID_HEADER));
            return Mono.empty();
        };

        traceFilter.filter(exchange1, chain1).block();
        assertNotNull(passedTraceId1.get());
        assertFalse(passedTraceId1.get().isEmpty());

        // 2. 有 TraceId，直接保留
        String existingTraceId = "custom-trace-id-12345";
        MockServerWebExchange exchange2 = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").header(ContextConstants.TRACE_ID_HEADER, existingTraceId).build()
        );
        AtomicReference<String> passedTraceId2 = new AtomicReference<>();
        WebFilterChain chain2 = ex -> {
            passedTraceId2.set(ex.getRequest().getHeaders().getFirst(ContextConstants.TRACE_ID_HEADER));
            return Mono.empty();
        };

        traceFilter.filter(exchange2, chain2).block();
        assertEquals(existingTraceId, passedTraceId2.get());
    }

    @Test
    @DisplayName("测试 ContextPathFilter 路径重写逻辑")
    void testContextPathFilter() {
        ServerProperties serverProperties = new ServerProperties();
        ContextPathFilter filter = new ContextPathFilter(serverProperties);

        // 1. contextPath 为空时直接放行
        MockServerWebExchange ex1 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/user").build());
        AtomicReference<String> path1 = new AtomicReference<>();
        filter.filter(ex1, ex -> {
            path1.set(ex.getRequest().getPath().value());
            return Mono.empty();
        }).block();
        assertEquals("/api/v1/user", path1.get());

        // 2. contextPath 为 /api，请求路径不匹配时直接放行
        serverProperties.getServlet().setContextPath("/api");
        MockServerWebExchange ex2 = MockServerWebExchange.from(MockServerHttpRequest.get("/other/path").build());
        AtomicReference<String> path2 = new AtomicReference<>();
        filter.filter(ex2, ex -> {
            path2.set(ex.getRequest().getPath().value());
            return Mono.empty();
        }).block();
        assertEquals("/other/path", path2.get());

        // 3. contextPath 为 /api，请求路径匹配时剥除前缀
        MockServerWebExchange ex3 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/user").build());
        AtomicReference<String> path3 = new AtomicReference<>();
        filter.filter(ex3, ex -> {
            path3.set(ex.getRequest().getPath().value());
            return Mono.empty();
        }).block();
        assertEquals("/v1/user", path3.get());
    }
}
