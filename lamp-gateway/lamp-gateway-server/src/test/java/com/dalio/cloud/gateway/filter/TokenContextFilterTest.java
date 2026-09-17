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

import cn.dev33.satoken.stp.StpUtil;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("测试用户 Token 解析及上下文请求头注入")
    void testParseTokenSuccess() {
        cn.dev33.satoken.session.SaSession mockSession = Mockito.mock(cn.dev33.satoken.session.SaSession.class);
        when(mockSession.getLoginId()).thenReturn("1001");
        when(mockSession.get(ContextConstants.JWT_KEY_EMPLOYEE_ID)).thenReturn("2001");
        when(mockSession.get(ContextConstants.JWT_KEY_TOP_COMPANY_ID)).thenReturn("3001");
        when(mockSession.get(ContextConstants.JWT_KEY_COMPANY_ID)).thenReturn("4001");
        when(mockSession.get(ContextConstants.JWT_KEY_DEPT_ID)).thenReturn("5001");

        try (org.mockito.MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            mockedStp.when(() -> StpUtil.getTokenSessionByToken("valid_token")).thenReturn(mockSession);

            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/user/detail")
                            .header("token", "valid_token")
                            .build()
            );

            AtomicReference<String> userIdHeader = new AtomicReference<>();
            AtomicReference<String> empIdHeader = new AtomicReference<>();
            WebFilterChain chain = ex -> {
                userIdHeader.set(ex.getRequest().getHeaders().getFirst(ContextConstants.USER_ID_HEADER));
                empIdHeader.set(ex.getRequest().getHeaders().getFirst(ContextConstants.EMPLOYEE_ID_HEADER));
                return Mono.empty();
            };

            filter.filter(exchange, chain).block();
            assertEquals("1001", userIdHeader.get());
            assertEquals("2001", empIdHeader.get());
        }
    }

    @Test
    @DisplayName("测试异常拦截与不同 HTTP 状态码转换")
    void testFilterExceptions() {
        // 1. UnauthorizedException
        TokenContextFilter unauthFilter = new TokenContextFilter(saTokenConfig, ignoreProperties) {
            @Override
            protected boolean isIgnoreToken(org.springframework.http.server.reactive.ServerHttpRequest request) {
                throw new com.dalio.basic.exception.UnauthorizedException(401, "Unauthorized token");
            }
        };
        MockServerWebExchange ex1 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure").build());
        unauthFilter.filter(ex1, ex -> Mono.empty()).block();
        assertEquals(HttpStatus.UNAUTHORIZED, ex1.getResponse().getStatusCode());

        // 2. BizException
        TokenContextFilter bizFilter = new TokenContextFilter(saTokenConfig, ignoreProperties) {
            @Override
            protected boolean isIgnoreToken(org.springframework.http.server.reactive.ServerHttpRequest request) {
                throw new com.dalio.basic.exception.BizException(400, "Biz error");
            }
        };
        MockServerWebExchange ex2 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure").build());
        bizFilter.filter(ex2, ex -> Mono.empty()).block();
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getResponse().getStatusCode());

        // 3. SaTokenException
        TokenContextFilter saFilter = new TokenContextFilter(saTokenConfig, ignoreProperties) {
            @Override
            protected boolean isIgnoreToken(org.springframework.http.server.reactive.ServerHttpRequest request) {
                throw new cn.dev33.satoken.exception.NotLoginException("Not logged in", "login", "token");
            }
        };
        MockServerWebExchange ex3 = MockServerWebExchange.from(MockServerHttpRequest.get("/api/secure").build());
        saFilter.filter(ex3, ex -> Mono.empty()).block();
        assertEquals(HttpStatus.UNAUTHORIZED, ex3.getResponse().getStatusCode());
    }
}
