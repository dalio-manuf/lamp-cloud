package com.dalio.cloud.common.properties;

import cn.hutool.core.collection.CollUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IgnoreProperties 单元测试
 */
class IgnorePropertiesTest {

    private IgnoreProperties ignoreProperties;

    @BeforeEach
    void setUp() {
        ignoreProperties = new IgnoreProperties();
        Map<String, Set<String>> anyone = new HashMap<>();
        anyone.put("GET", CollUtil.newHashSet("/api/anyone/test", "/api/wildcard/**"));
        ignoreProperties.setAnyone(anyone);

        Map<String, Set<String>> anyUser = new HashMap<>();
        anyUser.put("POST", CollUtil.newHashSet("/api/anyUser/login"));
        ignoreProperties.setAnyUser(anyUser);

        Map<String, Set<String>> anyTenant = new HashMap<>();
        anyTenant.put("ALL", CollUtil.newHashSet("/api/anyTenant/public"));
        ignoreProperties.setAnyTenant(anyTenant);
    }

    @Test
    @DisplayName("测试基础配置和默认值")
    void testDefaults() {
        assertTrue(ignoreProperties.getAuthEnabled());
        assertFalse(ignoreProperties.getCaseSensitive());
        assertFalse(ignoreProperties.getNotConfigUriAllow());
        assertNotNull(ignoreProperties.getBaseUri());
        assertFalse(ignoreProperties.getBaseUri().isEmpty());
    }

    @Test
    @DisplayName("测试构建联合路径映射")
    void testBuildMaps() {
        Map<String, Set<String>> buildAnyone = ignoreProperties.buildAnyone();
        assertNotNull(buildAnyone);
        assertTrue(buildAnyone.containsKey("ALL"));
        assertTrue(buildAnyone.containsKey("GET"));

        Map<String, Set<String>> buildAnyUser = ignoreProperties.buildAnyUser();
        assertNotNull(buildAnyUser);
        assertTrue(buildAnyUser.containsKey("POST"));

        Map<String, Set<String>> buildAnyTenant = ignoreProperties.buildAnyTenant();
        assertNotNull(buildAnyTenant);
    }

    @Test
    @DisplayName("测试 isIgnoreAnyone 判定")
    void testIsIgnoreAnyone() {
        assertFalse(ignoreProperties.isIgnoreAnyone("GET", null));
        // 匹配 baseUri 中预设的放行资源
        assertTrue(ignoreProperties.isIgnoreAnyone("GET", "/swagger-ui.html"));
        assertTrue(ignoreProperties.isIgnoreAnyone("GET", "/favicon.ico"));
        assertTrue(ignoreProperties.isIgnoreAnyone("GET", "/actuator/health"));
        // 匹配 anyone
        assertTrue(ignoreProperties.isIgnoreAnyone("GET", "/api/anyone/test"));
        assertTrue(ignoreProperties.isIgnoreAnyone("GET", "/api/wildcard/sub/path"));
        // 方法不匹配
        assertFalse(ignoreProperties.isIgnoreAnyone("POST", "/api/anyone/test"));
        // 匹配 anyTenant (ALL method)
        assertTrue(ignoreProperties.isIgnoreAnyone("DELETE", "/api/anyTenant/public"));
        // 未配置的路径
        assertFalse(ignoreProperties.isIgnoreAnyone("GET", "/api/secured/resource"));
    }

    @Test
    @DisplayName("测试 isIgnoreUser 判定")
    void testIsIgnoreUser() {
        assertFalse(ignoreProperties.isIgnoreUser("GET", null));
        assertTrue(ignoreProperties.isIgnoreUser("GET", "/swagger-ui.html"));
        assertTrue(ignoreProperties.isIgnoreUser("POST", "/api/anyUser/login"));
        assertFalse(ignoreProperties.isIgnoreUser("GET", "/api/anyUser/login"));
        // anyone 中的接口不应在 isIgnoreUser 中自动放行
        assertFalse(ignoreProperties.isIgnoreUser("GET", "/api/anyone/test"));
    }

    @Test
    @DisplayName("测试 isIgnoreTenant 判定")
    void testIsIgnoreTenant() {
        assertFalse(ignoreProperties.isIgnoreTenant("GET", null));
        assertTrue(ignoreProperties.isIgnoreTenant("GET", "/swagger-ui.html"));
        assertTrue(ignoreProperties.isIgnoreTenant("GET", "/api/anyTenant/public"));
        // anyUser 与 anyone 不应忽略租户
        assertFalse(ignoreProperties.isIgnoreTenant("POST", "/api/anyUser/login"));
        assertFalse(ignoreProperties.isIgnoreTenant("GET", "/api/anyone/test"));
    }

    @Test
    @DisplayName("测试 isIgnore 通用方法")
    void testIsIgnoreGeneral() {
        Map<String, Set<String>> map = Collections.singletonMap("GET", Collections.singleton("/test/**"));
        assertFalse(ignoreProperties.isIgnore("GET", (String) null, map));
        assertFalse(ignoreProperties.isIgnore("GET", "/test/abc", null));
        assertFalse(ignoreProperties.isIgnore("GET", "/test/abc", Collections.emptyMap()));
        assertTrue(ignoreProperties.isIgnore("GET", "/test/abc", map));
        assertFalse(ignoreProperties.isIgnore("POST", "/test/abc", map));
    }
}
