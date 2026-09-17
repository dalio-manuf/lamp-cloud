package com.dalio.cloud.gateway;

import com.dalio.cloud.GatewayServerApplication;
import com.dalio.cloud.common.ServerApplication;
import com.dalio.cloud.gateway.config.*;
import com.dalio.cloud.gateway.filter.GrayscaleReactiveLoadBalancerClientFilter;
import com.dalio.cloud.gateway.filter.OrderedConstant;
import com.dalio.cloud.gateway.rule.GrayVersionLoadBalancer;
import com.dalio.cloud.gateway.rule.GrayscaleLoadBalancer;
import feign.codec.Decoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 网关核心过滤器与配置类单元测试
 */
class GatewayFiltersAndConfigTest {

    @Test
    @DisplayName("测试 OpenApi3Controller 文档聚合逻辑与销毁")
    void testOpenApi3Controller() {
        GatewayProperties gatewayProperties = new GatewayProperties();
        RestTemplate restTemplate = mock(RestTemplate.class);
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);

        RouteDefinition route1 = new RouteDefinition();
        route1.setId("base-server");
        route1.setUri(URI.create("lb://lamp-base-server"));

        RouteDefinition route2 = new RouteDefinition();
        route2.setId("system-server");
        route2.setUri(URI.create("lb://lamp-system-server"));

        RouteDefinition invalidRoute = new RouteDefinition();
        invalidRoute.setId("invalid");

        gatewayProperties.setRoutes(Arrays.asList(route1, route2, invalidRoute));

        ServiceInstance instance = new DefaultServiceInstance("inst1", "lamp-base-server", "127.0.0.1", 8080, false);
        when(discoveryClient.getInstances("lamp-base-server")).thenReturn(Collections.singletonList(instance));
        when(discoveryClient.getInstances("lamp-system-server")).thenReturn(Collections.emptyList());

        Map<String, Object> swaggerDoc = new HashMap<>();
        List<Map<String, Object>> urls = new ArrayList<>();
        Map<String, Object> urlItem = new HashMap<>();
        urlItem.put("name", "基础服务");
        urlItem.put("url", "/v3/api-docs");
        urls.add(urlItem);
        swaggerDoc.put("urls", urls);

        when(restTemplate.getForObject(contains("lamp-base-server"), eq(Map.class))).thenReturn(swaggerDoc);

        OpenApi3Controller controller = new OpenApi3Controller(gatewayProperties, restTemplate, discoveryClient);

        // 验证 swaggerResources 聚合
        ResponseEntity<Map<String, Object>> entity = controller.swaggerResources().block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        Map<String, Object> body = entity.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("urls"));

        // 验证销毁
        controller.destroy();
    }

    @Test
    @DisplayName("测试 JsonExceptionHandler 异常拦截与响应渲染")
    void testJsonExceptionHandler() {
        JsonExceptionHandler handler = new JsonExceptionHandler();
        ServerCodecConfigurer codecConfigurer = new DefaultServerCodecConfigurer();
        handler.setMessageReaders(codecConfigurer.getReaders());
        handler.setMessageWriters(codecConfigurer.getWriters());
        handler.setViewResolvers(Collections.emptyList());

        // 1. NotFoundException
        MockServerWebExchange exchange1 = MockServerWebExchange.from(MockServerHttpRequest.get("/not-found").build());
        NotFoundException notFoundEx = NotFoundException.create(false, "service not found");
        handler.handle(exchange1, notFoundEx).block();
        assertEquals(HttpStatus.NOT_FOUND, exchange1.getResponse().getStatusCode());

        // 2. ResponseStatusException
        MockServerWebExchange exchange2 = MockServerWebExchange.from(MockServerHttpRequest.get("/bad-request").build());
        ResponseStatusException statusEx = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request Parameter");
        handler.handle(exchange2, statusEx).block();
        assertEquals(HttpStatus.BAD_REQUEST, exchange2.getResponse().getStatusCode());

        // 3. 通用 Exception
        MockServerWebExchange exchange3 = MockServerWebExchange.from(MockServerHttpRequest.get("/error").build());
        RuntimeException generalEx = new RuntimeException("Server Crash");
        handler.handle(exchange3, generalEx).block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange3.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("测试 GrayscaleReactiveLoadBalancerClientFilter 路由与灰度分发")
    void testGrayscaleFilter() {
        GatewayLoadBalancerProperties properties = new GatewayLoadBalancerProperties();
        properties.setUse404(true);
        GrayscaleLoadBalancer grayLoadBalancer = mock(GrayscaleLoadBalancer.class);

        GrayscaleReactiveLoadBalancerClientFilter filter = new GrayscaleReactiveLoadBalancerClientFilter(properties, grayLoadBalancer);
        assertEquals(OrderedConstant.GRAY, filter.getOrder());

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // 1. 无 GATEWAY_REQUEST_URL_ATTR
        MockServerWebExchange exchangeNoUrl = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
        filter.filter(exchangeNoUrl, chain).block();
        verify(chain, times(1)).filter(exchangeNoUrl);

        // 2. 非 lb scheme
        MockServerWebExchange exchangeHttp = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
        exchangeHttp.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("http://localhost:8080/test"));
        filter.filter(exchangeHttp, chain).block();

        // 3. lb scheme 但无可用实例 -> 抛出 NotFoundException
        MockServerWebExchange exchangeLbNoInstance = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
        exchangeLbNoInstance.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("lb://lamp-base-server/api/test"));
        when(grayLoadBalancer.choose(eq("lamp-base-server"), any())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> filter.filter(exchangeLbNoInstance, chain).block());

        // 4. lb scheme 且有可用实例
        MockServerWebExchange exchangeLbSuccess = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());
        exchangeLbSuccess.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("lb://lamp-base-server/api/test"));
        DefaultServiceInstance chosenInstance = new DefaultServiceInstance("inst1", "lamp-base-server", "192.168.1.100", 8080, false);
        when(grayLoadBalancer.choose(eq("lamp-base-server"), any())).thenReturn(chosenInstance);

        filter.filter(exchangeLbSuccess, chain).block();
        URI finalUrl = exchangeLbSuccess.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        assertNotNull(finalUrl);
        assertEquals("http", finalUrl.getScheme());
        assertEquals("192.168.1.100", finalUrl.getHost());
        assertEquals(8080, finalUrl.getPort());
    }

    @Test
    @DisplayName("测试 CorsConfiguration 跨域过滤器与 Bean 装配")
    void testCorsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        WebFilter corsFilter = corsConfig.corsFilter();
        assertNotNull(corsFilter);

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        // 1. 非跨域请求放行
        MockServerHttpRequest req1 = MockServerHttpRequest.get("http://localhost:8080/api/user").build();
        MockServerWebExchange ex1 = MockServerWebExchange.from(req1);
        corsFilter.filter(ex1, chain).block();

        // 2. 跨域 OPTIONS 预检请求
        MockServerHttpRequest req2 = MockServerHttpRequest.options("http://localhost:8080/api/user")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type")
                .build();
        MockServerWebExchange ex2 = MockServerWebExchange.from(req2);
        corsFilter.filter(ex2, chain).block();
        assertEquals(HttpStatus.OK, ex2.getResponse().getStatusCode());
        assertEquals("http://localhost:3000", ex2.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        // 3. 跨域 GET 请求
        MockServerHttpRequest req3 = MockServerHttpRequest.get("http://localhost:8080/api/user")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .build();
        MockServerWebExchange ex3 = MockServerWebExchange.from(req3);
        corsFilter.filter(ex3, chain).block();
        assertEquals("http://localhost:3000", ex3.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        // 4. 其他组件装配
        ReactiveDiscoveryClient reactiveDiscoveryClient = mock(ReactiveDiscoveryClient.class);
        when(reactiveDiscoveryClient.getServices()).thenReturn(reactor.core.publisher.Flux.empty());
        DiscoveryLocatorProperties discoveryLocatorProperties = new DiscoveryLocatorProperties();
        assertNotNull(corsConfig.discoveryClientRouteDefinitionLocator(reactiveDiscoveryClient, discoveryLocatorProperties));
        assertNotNull(corsConfig.serverCodecConfigurer());
        Decoder feignDecoder = corsConfig.feignFormDecoder();
        assertNotNull(feignDecoder);
    }

    @Test
    @DisplayName("测试 GatewayWebConfig 与 ExceptionConfiguration 配置装配")
    void testConfigs() {
        GatewayWebConfig webConfig = new GatewayWebConfig();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        assertNotNull(webConfig.getSpringUtils(applicationContext));

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        assertNotNull(webConfig.jacksonObjectMapper(builder));

        assertNotNull(webConfig.dateConvert());
        assertNotNull(webConfig.localDateConverter());
        assertNotNull(webConfig.localTimeConverter());
        assertNotNull(webConfig.localDateTimeConverter());

        // ExceptionConfiguration
        ExceptionConfiguration exConfig = new ExceptionConfiguration();
        @SuppressWarnings("unchecked")
        ObjectProvider<List<ViewResolver>> viewResolvers = mock(ObjectProvider.class);
        when(viewResolvers.getIfAvailable(any())).thenReturn(Collections.emptyList());
        ServerCodecConfigurer serverCodecConfigurer = new DefaultServerCodecConfigurer();
        assertNotNull(exConfig.errorWebExceptionHandler(viewResolvers, serverCodecConfigurer));

        // GrayscaleLoadBalancerClientConfig
        GrayscaleLoadBalancerClientConfig grayConfig = new GrayscaleLoadBalancerClientConfig();
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        GrayscaleLoadBalancer grayLb = grayConfig.grayLoadBalancer(discoveryClient);
        assertNotNull(grayLb);
        assertNotNull(grayConfig.gatewayLoadBalancerClientFilter(grayLb, new GatewayLoadBalancerProperties()));

        // GatewayServerApplication
        class TestApp extends GatewayServerApplication {
            TestApp() {
                super();
            }
        }
        assertNotNull(new TestApp());

        try (org.mockito.MockedStatic<ServerApplication> mocked = mockStatic(ServerApplication.class)) {
            assertDoesNotThrow(() -> GatewayServerApplication.main(new String[]{}));
            mocked.verify(() -> ServerApplication.start(eq(GatewayServerApplication.class), any(String[].class)));
        }
    }

    @Test
    @DisplayName("测试 GrayVersionLoadBalancer 灰度与随机负载均衡")
    void testGrayVersionLoadBalancer() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        GrayVersionLoadBalancer lb = new GrayVersionLoadBalancer(discoveryClient);

        // 1. 无实例 -> 抛出 NotFoundException
        when(discoveryClient.getInstances("empty-service")).thenReturn(Collections.emptyList());
        MockServerHttpRequest req1 = MockServerHttpRequest.get("http://localhost/api/test").build();
        assertThrows(NotFoundException.class, () -> lb.choose("empty-service", req1));

        // 2. 有实例但无 gray_version 头 -> 随机返回
        DefaultServiceInstance inst1 = new DefaultServiceInstance("i1", "test-service", "192.168.1.1", 8080, false);
        DefaultServiceInstance inst2 = new DefaultServiceInstance("i2", "test-service", "192.168.1.2", 8080, false);
        inst2.getMetadata().put(com.dalio.basic.context.ContextConstants.GRAY_VERSION, "v2");

        when(discoveryClient.getInstances("test-service")).thenReturn(Arrays.asList(inst1, inst2));
        MockServerHttpRequest req2 = MockServerHttpRequest.get("http://localhost/api/test").build();
        ServiceInstance chosenNoHeader = lb.choose("test-service", req2);
        assertNotNull(chosenNoHeader);

        // 3. 有 gray_version 头且精确匹配
        MockServerHttpRequest req3 = MockServerHttpRequest.get("http://localhost/api/test")
                .header(com.dalio.basic.context.ContextConstants.GRAY_VERSION, "v2")
                .build();
        ServiceInstance chosenMatched = lb.choose("test-service", req3);
        assertNotNull(chosenMatched);
        assertEquals("i2", chosenMatched.getInstanceId());

        // 4. 有 gray_version 头但无匹配 -> 降级随机返回
        MockServerHttpRequest req4 = MockServerHttpRequest.get("http://localhost/api/test")
                .header(com.dalio.basic.context.ContextConstants.GRAY_VERSION, "v999")
                .build();
        ServiceInstance chosenMismatch = lb.choose("test-service", req4);
        assertNotNull(chosenMismatch);
    }

    @Test
    @DisplayName("测试 MySwaggerXForwardedHeadersFilter 头部改写逻辑")
    void testMySwaggerXForwardedHeadersFilter() {
        com.dalio.cloud.gateway.filter.MySwaggerXForwardedHeadersFilter filter = new com.dalio.cloud.gateway.filter.MySwaggerXForwardedHeadersFilter();
        assertEquals(OrderedConstant.SWAGGER, filter.getOrder());
        assertTrue(filter.supports(org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter.Type.REQUEST));

        // 1. 无 URI 属性
        MockServerWebExchange ex1 = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/api/test").build());
        HttpHeaders headers1 = new HttpHeaders();
        HttpHeaders res1 = filter.filter(headers1, ex1);
        assertEquals(headers1, res1);

        // 2. 有 originalUris 和 requestUri
        MockServerWebExchange ex2 = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost/api/oauth/v3/api-docs").build());
        LinkedHashSet<URI> originalUris = new LinkedHashSet<>();
        originalUris.add(URI.create("http://localhost/api/oauth/v3/api-docs"));
        ex2.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR, originalUris);
        ex2.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("http://192.168.1.1:8080/v3/api-docs"));

        HttpHeaders headers2 = new HttpHeaders();
        HttpHeaders res2 = filter.filter(headers2, ex2);
        assertNotNull(res2);
    }
}
