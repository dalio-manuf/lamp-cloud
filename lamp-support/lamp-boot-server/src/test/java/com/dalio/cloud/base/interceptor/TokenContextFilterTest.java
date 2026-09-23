package com.dalio.cloud.base.interceptor;

import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.common.properties.IgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenContextFilterTest {

    private TokenContextFilter filter;
    private IgnoreProperties ignoreProperties;

    @BeforeEach
    void setUp() {
        ignoreProperties = new IgnoreProperties();
        filter = new TokenContextFilter("dev", ignoreProperties);
        ContextUtil.remove();
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试非 HandlerMethod 直接放行")
    void testPreHandleNonHandlerMethod() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = filter.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("测试正常解析 Client 和 Application 请求头")
    void testPreHandleSuccess() throws NoSuchMethodException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Method method = SampleHandler.class.getMethod("handle");
        HandlerMethod handlerMethod = new HandlerMethod(new SampleHandler(), method);

        String basicAuth = Base64.getEncoder().encodeToString("client_app:client_secret".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader(ContextConstants.CLIENT_KEY)).thenReturn(basicAuth);
        when(request.getHeader(ContextConstants.APPLICATION_ID_KEY)).thenReturn("999");
        when(request.getHeader(ContextConstants.PATH_HEADER)).thenReturn("/api/test");

        boolean result = filter.preHandle(request, response, handlerMethod);
        assertTrue(result);
        assertTrue(ContextUtil.getBoot());
        assertEquals("client_app", ContextUtil.getClientId());
        assertEquals(999L, ContextUtil.getApplicationId());
        assertEquals("/api/test", ContextUtil.getPath());
    }

    @Test
    @DisplayName("测试请求头不存在时从请求参数读取")
    void testPreHandleFromParameter() throws NoSuchMethodException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Method method = SampleHandler.class.getMethod("handle");
        HandlerMethod handlerMethod = new HandlerMethod(new SampleHandler(), method);

        when(request.getHeader(ContextConstants.CLIENT_KEY)).thenReturn(null);
        when(request.getParameter(ContextConstants.CLIENT_KEY)).thenReturn(null);
        when(request.getHeader(ContextConstants.APPLICATION_ID_KEY)).thenReturn(null);
        when(request.getParameter(ContextConstants.APPLICATION_ID_KEY)).thenReturn("888");

        boolean result = filter.preHandle(request, response, handlerMethod);
        assertTrue(result);
        assertEquals(888L, ContextUtil.getApplicationId());
        assertNull(ContextUtil.getClientId());
    }

    @Test
    @DisplayName("测试 isDev 方法与环境判断")
    void testIsDev() {
        assertTrue(filter.isDev("test"));
        assertTrue(filter.isDev(com.dalio.basic.utils.StrPool.TEST_TOKEN));
        assertFalse(filter.isDev("other_token"));

        TokenContextFilter prodFilter = new TokenContextFilter("prod", ignoreProperties);
        assertFalse(prodFilter.isDev("test"));
    }

    @Test
    @DisplayName("测试 afterCompletion 清理上下文")
    void testAfterCompletion() {
        ContextUtil.setClientId("temp_client");
        filter.afterCompletion(mock(HttpServletRequest.class), mock(HttpServletResponse.class), new Object(), null);
        assertNull(ContextUtil.getClientId());
    }

    public static class SampleHandler {
        public void handle() {
        }
    }
}
