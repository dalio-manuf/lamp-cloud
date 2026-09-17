package com.dalio.cloud.userinfo.resolver;

import com.dalio.basic.annotation.user.LoginUser;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.userinfo.service.UserResolverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ContextArgumentResolverTest {

    private ContextArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ContextArgumentResolver();
        ContextUtil.remove();
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    public static class TestController {
        public void withLoginUserFull(@LoginUser(isFull = true) SysUser user) {}
        public void withLoginUserDefault(@LoginUser SysUser user) {}
        public void withoutAnnotation(SysUser user) {}
        public void withDifferentType(@LoginUser String text) {}
    }

    private MethodParameter getMethodParameter(String methodName) throws NoSuchMethodException {
        for (Method m : TestController.class.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return new MethodParameter(m, 0);
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @Test
    @DisplayName("测试 supportsParameter 方法")
    void testSupportsParameter() throws NoSuchMethodException {
        assertTrue(resolver.supportsParameter(getMethodParameter("withLoginUserFull")));
        assertTrue(resolver.supportsParameter(getMethodParameter("withLoginUserDefault")));
        assertFalse(resolver.supportsParameter(getMethodParameter("withoutAnnotation")));
        assertFalse(resolver.supportsParameter(getMethodParameter("withDifferentType")));
    }

    @Test
    @DisplayName("测试用户ID为空时直接返回基础 SysUser")
    void testResolveArgumentWhenUserIdNull() throws NoSuchMethodException {
        ContextUtil.setEmployeeId(20L);
        MethodParameter mp = getMethodParameter("withLoginUserFull");
        NativeWebRequest request = mock(NativeWebRequest.class);

        Object result = resolver.resolveArgument(mp, null, request, null);
        assertNotNull(result);
        assertTrue(result instanceof SysUser);
        SysUser user = (SysUser) result;
        assertNull(user.getId());
        assertEquals(20L, user.getEmployeeId());
    }

    @Test
    @DisplayName("测试没有注解时直接返回 SysUser")
    void testResolveArgumentWithoutAnnotation() throws NoSuchMethodException {
        ContextUtil.setUserId(1L);
        ContextUtil.setEmployeeId(20L);
        MethodParameter mp = getMethodParameter("withoutAnnotation");
        NativeWebRequest request = mock(NativeWebRequest.class);

        Object result = resolver.resolveArgument(mp, null, request, null);
        assertNotNull(result);
        SysUser user = (SysUser) result;
        assertEquals(1L, user.getId());
        assertEquals(20L, user.getEmployeeId());
    }

    @Test
    @DisplayName("测试全量查询成功返回")
    void testResolveArgumentFullSuccess() throws NoSuchMethodException {
        ContextUtil.setUserId(1L);
        ContextUtil.setEmployeeId(20L);
        MethodParameter mp = getMethodParameter("withLoginUserFull");
        NativeWebRequest request = mock(NativeWebRequest.class);

        UserResolverService mockService = mock(UserResolverService.class);
        SysUser enrichedUser = new SysUser();
        enrichedUser.setId(1L);
        enrichedUser.setEmployeeId(20L);
        enrichedUser.setUsername("testUser");

        when(mockService.getById(any(UserQuery.class))).thenReturn(R.success(enrichedUser));

        try (MockedStatic<SpringUtils> mockedSpringUtils = mockStatic(SpringUtils.class)) {
            mockedSpringUtils.when(() -> SpringUtils.getBean(eq(UserResolverService.class))).thenReturn(mockService);

            Object result = resolver.resolveArgument(mp, null, request, null);
            assertNotNull(result);
            assertEquals("testUser", ((SysUser) result).getUsername());
        }
    }

    @Test
    @DisplayName("测试查询异常时降级返回未丰富对象")
    void testResolveArgumentExceptionFallback() throws NoSuchMethodException {
        ContextUtil.setUserId(1L);
        ContextUtil.setEmployeeId(20L);
        MethodParameter mp = getMethodParameter("withLoginUserFull");
        NativeWebRequest request = mock(NativeWebRequest.class);

        try (MockedStatic<SpringUtils> mockedSpringUtils = mockStatic(SpringUtils.class)) {
            mockedSpringUtils.when(() -> SpringUtils.getBean(eq(UserResolverService.class)))
                    .thenThrow(new RuntimeException("Bean not found"));

            Object result = resolver.resolveArgument(mp, null, request, null);
            assertNotNull(result);
            SysUser user = (SysUser) result;
            assertEquals(1L, user.getId());
            assertEquals(20L, user.getEmployeeId());
        }
    }

    @Test
    @DisplayName("测试默认参数且无查询标志时")
    void testResolveArgumentDefaultNoQuery() throws NoSuchMethodException {
        ContextUtil.setUserId(1L);
        ContextUtil.setEmployeeId(20L);
        MethodParameter mp = getMethodParameter("withLoginUserDefault");
        NativeWebRequest request = mock(NativeWebRequest.class);

        Object result = resolver.resolveArgument(mp, null, request, null);
        assertNotNull(result);
        SysUser user = (SysUser) result;
        assertEquals(1L, user.getId());
        assertEquals(20L, user.getEmployeeId());
    }
}
