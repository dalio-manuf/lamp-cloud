package com.dalio.cloud.gateway.controller;

import com.dalio.basic.base.R;
import com.dalio.cloud.gateway.fallback.FallbackController;
import com.dalio.cloud.model.vo.result.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.result.view.Rendering;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Gateway 控制器单元测试
 */
class GatewayControllersTest {

    @Test
    @DisplayName("测试 FallbackController 超时熔断回调")
    void testFallback() {
        FallbackController fallbackController = new FallbackController();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/timeout").build());
        R<String> result = fallbackController.fallback(exchange).block();

        assertNotNull(result);
        assertFalse(result.getIsSuccess());
    }

    @Test
    @DisplayName("测试 GateController 在线服务路由发现与文档跳转")
    void testGateController() {
        DiscoveryClient discoveryClient = Mockito.mock(DiscoveryClient.class);
        GatewayProperties gatewayProperties = new GatewayProperties();

        RouteDefinition route = new RouteDefinition();
        route.setId("oauth-route");
        route.setUri(URI.create("lb://lamp-oauth-server"));

        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        Map<String, String> args = new HashMap<>();
        args.put("_genkey_0", "/oauth/**");
        predicate.setArgs(args);
        route.setPredicates(Collections.singletonList(predicate));

        gatewayProperties.setRoutes(Collections.singletonList(route));

        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("lamp-oauth-server"));

        GateController controller = new GateController(discoveryClient, gatewayProperties);

        // 1. doc 跳转
        Rendering rendering = controller.doc();
        assertNotNull(rendering);

        // 2. findOnlineServicePrefix
        R<Map<String, String>> prefixRes = controller.findOnlineServicePrefix();
        assertNotNull(prefixRes);
        assertTrue(prefixRes.getIsSuccess());
        assertEquals("oauth", prefixRes.getData().get("lamp-oauth-server"));

        // 3. findOnlineService
        R<List<Option>> onlineRes = controller.findOnlineService();
        assertNotNull(onlineRes);
        assertTrue(onlineRes.getIsSuccess());
        assertEquals(1, onlineRes.getData().size());
        assertEquals("oauth", onlineRes.getData().get(0).getValue());
    }
}
