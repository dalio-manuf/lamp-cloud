package com.dalio.cloud.base.interceptor;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.system.facade.DefResourceFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AuthenticationSaInterceptorTest {

    private AuthenticationSaInterceptor interceptor;
    private IgnoreProperties ignoreProperties;
    private DefResourceFacade defResourceFacade;

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
        cn.dev33.satoken.context.mock.SaRequestForMock mockReq =
                (cn.dev33.satoken.context.mock.SaRequestForMock) cn.dev33.satoken.context.SaHolder.getRequest();
        mockReq.requestPath = "/api/data";
        mockReq.method = "GET";
        cn.dev33.satoken.strategy.SaStrategy.instance.routeMatcher = (pattern, path) -> {
            if (path == null) {
                return false;
            }
            return cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil.match(pattern, path);
        };
        ignoreProperties = new IgnoreProperties();
        ignoreProperties.setNotConfigUriAllow(true);
        defResourceFacade = mock(DefResourceFacade.class);
        interceptor = new AuthenticationSaInterceptor(ignoreProperties, defResourceFacade);
        ContextUtil.remove();
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试非 HandlerMethod 直接放行")
    void testPreHandleNonHandlerMethod() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("测试 preHandle 解析 Token 并写入 ContextUtil")
    void testPreHandleAndParseToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/data");

        Method method = SampleHandler.class.getMethod("handle");
        HandlerMethod handlerMethod = new HandlerMethod(new SampleHandler(), method);

        SaSession mockSession = mock(SaSession.class);
        when(mockSession.getLoginId()).thenReturn(100L);
        when(mockSession.get(ContextConstants.JWT_KEY_TOP_COMPANY_ID)).thenReturn(200L);
        when(mockSession.get(ContextConstants.JWT_KEY_COMPANY_ID)).thenReturn(300L);
        when(mockSession.get(ContextConstants.JWT_KEY_DEPT_ID)).thenReturn(400L);
        when(mockSession.get(ContextConstants.JWT_KEY_EMPLOYEE_ID)).thenReturn(500L);

        try (MockedStatic<StpUtil> mockedStp = mockStatic(StpUtil.class)) {
            mockedStp.when(StpUtil::getTokenSession).thenReturn(mockSession);

            // 当 ignoreProperties 忽略用户时
            ignoreProperties.getAnyUser().computeIfAbsent("ALL", k -> new java.util.HashSet<>()).add("/api/data");
            boolean ignoreResult = interceptor.preHandle(request, response, handlerMethod);
            assertTrue(ignoreResult);

            // 当正常解析 token 时
            ignoreProperties.getAnyUser().clear();
            boolean normalResult = interceptor.preHandle(request, response, handlerMethod);
            assertTrue(normalResult);
            assertEquals(100L, ContextUtil.getUserId());
            assertEquals(500L, ContextUtil.getEmployeeId());
            assertEquals(300L, ContextUtil.getCurrentCompanyId());
            assertEquals(200L, ContextUtil.getCurrentTopCompanyId());
            assertEquals(400L, ContextUtil.getCurrentDeptId());
        }
    }

    @Test
    @DisplayName("测试 Auth 路由认证与未配置接口校验分支")
    @SuppressWarnings("unchecked")
    void testAuthLogic() {
        SaParamFunction<Object> auth = (SaParamFunction<Object>) ReflectionTestUtils.getField(interceptor, "auth");

        Map<String, Set<String>> allApi = new HashMap<>();
        allApi.put("/api/users###GET", Collections.singleton("sys:user:view"));
        when(defResourceFacade.listAllApi()).thenReturn(allApi);

        // 设置未配置接口不允许访问
        ignoreProperties.setNotConfigUriAllow(false);
        cn.dev33.satoken.context.mock.SaRequestForMock mockReq =
                (cn.dev33.satoken.context.mock.SaRequestForMock) cn.dev33.satoken.context.SaHolder.getRequest();
        mockReq.requestPath = "/api/unknown";
        mockReq.method = "GET";

        // 模拟未配置且非白名单的接口访问，预期抛出 NotPermissionException
        try (MockedStatic<StpUtil> mockedStp = mockStatic(StpUtil.class)) {
            mockedStp.when(StpUtil::checkLogin).thenAnswer(inv -> null);
            assertThrows(NotPermissionException.class, () -> auth.run(new Object()));
        }

        // 允许未配置接口通过
        ignoreProperties.setNotConfigUriAllow(true);
        try (MockedStatic<StpUtil> mockedStp = mockStatic(StpUtil.class)) {
            mockedStp.when(StpUtil::checkLogin).thenAnswer(inv -> null);
            assertDoesNotThrow(() -> auth.run(new Object()));
        }
    }

    public static class SampleHandler {
        public void handle() {
        }
    }
}
