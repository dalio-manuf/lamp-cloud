package com.dalio.cloud.gateway.filter;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dalio.basic.context.ContextConstants;
import com.dalio.cloud.common.properties.IgnoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenContextFilter 单元测试
 */
class TokenContextFilterTest {

    private SaTokenConfig saTokenConfig;
    private IgnoreProperties ignoreProperties;
    private TokenContextFilter filter;

    @BeforeEach
    void setUp() {
        saTokenConfig = new SaTokenConfig();
        saTokenConfig.setTokenName("token");
        ignoreProperties = new IgnoreProperties();
        filter = new TokenContextFilter(saTokenConfig, ignoreProperties);
    }

    @Test
    @DisplayName("测试过滤顺序与基础白名单接口放行")
    void testBasicFilter() {
        assertEquals(OrderedConstant.TOKEN, filter.getOrder());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/anno/demo").build()
        );
        AtomicBoolean called = new AtomicBoolean(false);
        WebFilterChain chain = ex -> {
            called.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertTrue(called.get());
    }

    @Test
    @DisplayName("测试客户端 Basic 认证头解析与应用ID解析")
    void testClientAndApplicationParsing() {
        String clientRaw = "clientApp:secret123";
        String encodedClient = Base64.getEncoder().encodeToString(clientRaw.getBytes(StandardCharsets.UTF_8));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/anno/test")
                        .header(ContextConstants.CLIENT_KEY, "Basic " + encodedClient)
                        .header(ContextConstants.APPLICATION_ID_KEY, "9999")
                        .header(ContextConstants.GRAY_VERSION, "v2")
                        .build()
        );

        AtomicReference<String> passedClientId = new AtomicReference<>();
        AtomicReference<String> passedAppId = new AtomicReference<>();
        WebFilterChain chain = ex -> {
            passedClientId.set(ex.getRequest().getHeaders().getFirst(ContextConstants.CLIENT_ID_HEADER));
            passedAppId.set(ex.getRequest().getHeaders().getFirst(ContextConstants.APPLICATION_ID_HEADER));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertEquals("clientApp", passedClientId.get());
        assertEquals("9999", passedAppId.get());
    }

    @Test
    @DisplayName("测试 errorResponse 返回标准的 JSON 格式")
    void testErrorResponseFormat() {
        MockServerHttpResponse response = new MockServerHttpResponse();
        filter.errorResponse(response, "Token Expired", 40101, HttpStatus.UNAUTHORIZED).block();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        String body = response.getBodyAsString().block(java.time.Duration.ofSeconds(2));
        assertNotNull(body);
        assertTrue(JSONUtil.isTypeJSON(body), "响应内容必须为有效的 JSON 格式");

        JSONObject json = JSONUtil.parseObj(body);
        assertEquals(40101, json.getInt("code"));
        assertEquals("Token Expired", json.getStr("msg"));
    }
}
