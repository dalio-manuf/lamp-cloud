package com.dalio.cloud.generator.utils;

import com.dalio.cloud.generator.rules.NamingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NamingStrategy 和 ProjectUtils 工具类单元测试
 */
class ProjectUtilsTest {

    // ── NamingStrategy ──────────────────────────────────────────────────────

    @Test
    @DisplayName("underlineToCamel: 空字符串返回空")
    void testUnderlineToCamelBlank() {
        assertEquals("", NamingStrategy.underlineToCamel(null));
        assertEquals("", NamingStrategy.underlineToCamel(""));
        assertEquals("", NamingStrategy.underlineToCamel("  "));
    }

    @Test
    @DisplayName("underlineToCamel: 标准下划线转驼峰")
    void testUnderlineToCamelNormal() {
        assertEquals("baseOperationLog", NamingStrategy.underlineToCamel("base_operation_log"));
        assertEquals("defUser", NamingStrategy.underlineToCamel("def_user"));
        assertEquals("id", NamingStrategy.underlineToCamel("id"));
    }

    @Test
    @DisplayName("underlineToCamel: 全大写字符串先转小写再驼峰")
    void testUnderlineToCamelCapital() {
        // CAPITAL_MODE -> lowercase first, then camel
        assertEquals("baseUser", NamingStrategy.underlineToCamel("BASE_USER"));
    }

    @Test
    @DisplayName("removePrefix: 匹配前缀时去掉前缀")
    void testRemovePrefix() {
        List<String> prefix = Arrays.asList("base_", "def_");
        assertEquals("operation_log", NamingStrategy.removePrefix("base_operation_log", prefix));
        assertEquals("user", NamingStrategy.removePrefix("def_user", prefix));
        // 无匹配时原样返回
        assertEquals("sys_user", NamingStrategy.removePrefix("sys_user", prefix));
    }

    @Test
    @DisplayName("removePrefix: 空表名返回空串")
    void testRemovePrefixBlank() {
        assertEquals("", NamingStrategy.removePrefix("", List.of("def_")));
        assertEquals("", NamingStrategy.removePrefix(null, List.of("def_")));
    }

    @Test
    @DisplayName("removeSuffix: 匹配后缀时去掉后缀")
    void testRemoveSuffix() {
        List<String> suffix = List.of("_log", "_info");
        assertEquals("base_operation", NamingStrategy.removeSuffix("base_operation_log", suffix));
        assertEquals("user", NamingStrategy.removeSuffix("user_info", suffix));
        assertEquals("user_data", NamingStrategy.removeSuffix("user_data", suffix));
    }

    @Test
    @DisplayName("removePrefixAndCamel: 去前缀后再驼峰")
    void testRemovePrefixAndCamel() {
        assertEquals("operationLog", NamingStrategy.removePrefixAndCamel("base_operation_log", List.of("base_")));
    }

    @Test
    @DisplayName("capitalFirst: 首字母大写")
    void testCapitalFirst() {
        assertEquals("User", NamingStrategy.capitalFirst("user"));
        assertEquals("", NamingStrategy.capitalFirst(""));
        assertEquals("", NamingStrategy.capitalFirst(null));
        assertEquals("ABC", NamingStrategy.capitalFirst("ABC"));
    }
}
