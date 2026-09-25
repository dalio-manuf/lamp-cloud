package com.dalio.cloud.common.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.dalio.basic.database.properties.DatabaseProperties;
import com.dalio.basic.log.event.SysLogListener;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.aspect.LampLogAspect;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.datascope.interceptor.DataScopeInnerInterceptor;
import com.dalio.cloud.oauth.facade.LogFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CommonConfigBeansTest {

    @Test
    @DisplayName("测试 CommonAutoConfiguration 初始化与 Bean 创建")
    void testCommonAutoConfiguration() {
        SystemProperties properties = new SystemProperties();
        properties.setCachePrefix("test_cache:");

        CommonAutoConfiguration config = new CommonAutoConfiguration(properties);
        LampLogAspect logAspect = config.getLampLogAspect();
        assertNotNull(logAspect);

        config.init();
        assertEquals("test_cache:", CacheKeyBuilder.Key.getPrefix());

        // 测试空前缀分支
        properties.setCachePrefix("");
        config.init();
    }

    @Test
    @DisplayName("测试 ExceptionConfiguration 实例化")
    void testExceptionConfiguration() {
        ExceptionConfiguration exceptionConfig = new ExceptionConfiguration();
        assertNotNull(exceptionConfig);
    }

    @Test
    @DisplayName("测试 MybatisAutoConfiguration 拦截器与 DataScope 配置")
    void testMybatisAutoConfiguration() {
        DatabaseProperties dbProperties = new DatabaseProperties();
        dbProperties.setIsDataScope(true);

        MybatisAutoConfiguration mybatisConfig = new MybatisAutoConfiguration(dbProperties);
        DataScopeInnerInterceptor dataScopeInterceptor = mybatisConfig.getDataScopeInnerInterceptor();
        assertNotNull(dataScopeInterceptor);

        List<InnerInterceptor> interceptorsWithDataScope = mybatisConfig.getPaginationBeforeInnerInterceptor();
        assertNotNull(interceptorsWithDataScope);
        boolean hasDataScope = interceptorsWithDataScope.stream()
                .anyMatch(i -> i instanceof DataScopeInnerInterceptor);
        assertTrue(hasDataScope);

        // 测试关闭 dataScope
        dbProperties.setIsDataScope(false);
        List<InnerInterceptor> interceptorsWithoutDataScope = mybatisConfig.getPaginationBeforeInnerInterceptor();
        boolean hasDataScopeFalse = interceptorsWithoutDataScope.stream()
                .anyMatch(i -> i instanceof DataScopeInnerInterceptor);
        assertFalse(hasDataScopeFalse);
    }

    @Test
    @DisplayName("测试 WebConfiguration 与 SysLogListener 创建")
    void testWebConfiguration() {
        WebConfiguration webConfig = new WebConfiguration();
        assertNotNull(webConfig);

        LogFacade mockLogFacade = mock(LogFacade.class);
        SysLogListener listener = webConfig.sysLogListener(mockLogFacade);
        assertNotNull(listener);
    }

    @Test
    @DisplayName("测试 ActuatorSecurityConfig 密码加密器与用户服务")
    void testActuatorSecurityConfig() {
        ActuatorSecurityConfig securityConfig = new ActuatorSecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "actuatorUsername", "admin-tester");
        ReflectionTestUtils.setField(securityConfig, "actuatorPassword", "pass-123");

        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);

        UserDetailsService userDetailsService = securityConfig.userDetailsService(encoder);
        assertNotNull(userDetailsService);

        UserDetails user = userDetailsService.loadUserByUsername("admin-tester");
        assertNotNull(user);
        assertEquals("admin-tester", user.getUsername());
        assertTrue(encoder.matches("pass-123", user.getPassword()));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ACTUATOR_ADMIN")));

        org.springframework.security.config.annotation.web.builders.HttpSecurity http =
                mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        org.springframework.security.web.DefaultSecurityFilterChain chain = mock(org.springframework.security.web.DefaultSecurityFilterChain.class);
        try {
            org.mockito.Mockito.when(http.build()).thenReturn(chain);
            org.springframework.security.web.SecurityFilterChain result = securityConfig.securityFilterChain(http);
            assertNotNull(result);
        } catch (Exception e) {
            // ignore
        }
    }
}
