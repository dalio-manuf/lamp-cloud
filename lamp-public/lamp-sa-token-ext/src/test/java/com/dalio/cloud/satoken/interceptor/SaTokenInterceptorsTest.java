package com.dalio.cloud.satoken.interceptor;

import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.satoken.config.AlwaysConfigurer;
import com.dalio.cloud.satoken.config.GlobalMvcConfigurer;
import com.dalio.cloud.satoken.config.MySaTokenContextRegister;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SaToken 拦截器与配置类单元测试
 */
class SaTokenInterceptorsTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试 HeaderThreadLocalInterceptor 正常设置与非 HandlerMethod 忽略")
    void testHeaderThreadLocalInterceptor() {
        HeaderThreadLocalInterceptor interceptor = new HeaderThreadLocalInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 1. 非 HandlerMethod 放行且不处理
        boolean nonHandlerResult = interceptor.preHandle(request, response, new Object());
        assertTrue(nonHandlerResult);
        assertNull(ContextUtil.getUserId());

        // 2. 正常 HandlerMethod
        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
        request.addHeader(ContextConstants.JWT_KEY_USER_ID, "1001");
        request.addHeader(ContextConstants.EMPLOYEE_ID_HEADER, "2001");
        request.addHeader(ContextConstants.APPLICATION_ID_HEADER, "3001");
        request.addHeader(ContextConstants.CLIENT_ID_HEADER, "client_app");
        request.addHeader(ContextConstants.TRACE_ID_HEADER, "trace-abc-123");
        request.addHeader(ContextConstants.GRAY_VERSION, "1.0.0");
        request.addHeader(ContextConstants.CURRENT_COMPANY_ID_HEADER, "5001");
        request.addHeader(ContextConstants.CURRENT_DEPT_ID_HEADER, "6001");
        request.addHeader(ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER, "7001");

        boolean result = interceptor.preHandle(request, response, handlerMethod);
        assertTrue(result);
        assertEquals(1001L, ContextUtil.getUserId());
        assertEquals(2001L, ContextUtil.getEmployeeId());
        assertEquals(3001L, ContextUtil.getApplicationId());
        assertEquals("client_app", ContextUtil.getClientId());
        assertEquals("trace-abc-123", ContextUtil.getLogTraceId());
        assertEquals("1.0.0", ContextUtil.getGrayVersion());
        assertEquals(5001L, ContextUtil.getCurrentCompanyId());
        assertEquals(6001L, ContextUtil.getCurrentDeptId());
        assertEquals(7001L, ContextUtil.getCurrentTopCompanyId());

        // 3. completion 清理
        interceptor.afterCompletion(request, response, handlerMethod, null);
        assertNull(ContextUtil.getUserId());
    }

    @Test
    @DisplayName("测试 NotAllowWriteInterceptor 禁止写入拦截逻辑")
    void testNotAllowWriteInterceptor() throws Exception {
        SystemProperties properties = new SystemProperties();
        NotAllowWriteInterceptor interceptor = new NotAllowWriteInterceptor(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);

        // 默认未开启 notAllowWrite
        request.setMethod("POST");
        request.setRequestURI("/api/user/save");
        assertTrue(interceptor.preHandle(request, response, handlerMethod));

        // 开启 notAllowWrite
        properties.setNotAllowWrite(true);
        Map<String, List<String>> notAllowMap = new HashMap<>();
        notAllowMap.put("POST", Collections.singletonList("/api/user/**"));
        properties.setNotAllowWriteList(notAllowMap);

        // 命中禁止规则抛异常
        assertThrows(BizException.class, () -> interceptor.preHandle(request, response, handlerMethod));

        // 未命中禁止规则放行
        request.setRequestURI("/api/query/list");
        assertTrue(interceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    @DisplayName("测试 WebMvcConfigurer 与 MySaTokenContextRegister 配置初始化")
    void testConfigurers() {
        SystemProperties properties = new SystemProperties();
        InterceptorRegistry registry = new InterceptorRegistry();

        GlobalMvcConfigurer globalMvcConfigurer = new GlobalMvcConfigurer();
        globalMvcConfigurer.addInterceptors(registry);

        AlwaysConfigurer alwaysConfigurer = new AlwaysConfigurer(properties);
        alwaysConfigurer.addInterceptors(registry);

        MySaTokenContextRegister register = new MySaTokenContextRegister();
        assertNotNull(register.getAlwaysConfigurer(properties));

        MySaTokenContextRegister.InnerConfig innerConfig = new MySaTokenContextRegister.InnerConfig();
        assertNotNull(innerConfig.getGlobalMvcConfigurer());
    }
}
