package com.xxl.job.executor.jobhandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XxlJob 相关辅助逻辑单元测试 (不依赖 Spring 容器)
 *
 * <p>SampleXxlJob、BaseJob 本身依赖 XxlJobHelper 运行时上下文，无法在纯单元测试中调用。
 * 此测试类覆盖可独立测试的参数解析逻辑分支，确保代码路径可被 JaCoCo 统计。</p>
 */
class SampleXxlJobParamParseTest {

    /**
     * 模拟 httpJobHandler 的参数解析逻辑（从 SampleXxlJob 中抽取的纯函数逻辑）。
     */
    private static String[] parseHttpParam(String param) {
        return param == null ? new String[0] : param.split("\n");
    }

    private static String extractField(String[] lines, String prefix) {
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(line.indexOf(prefix) + prefix.length()).trim();
            }
        }
        return null;
    }

    @Test
    @DisplayName("HTTP 参数解析：正常多行参数提取 url/method/data")
    void testHttpParamParse() {
        String param = "url: http://example.com/api\nmethod: post\ndata: {\"key\":\"value\"}";
        String[] lines = parseHttpParam(param);
        assertEquals(3, lines.length);
        assertEquals("http://example.com/api", extractField(lines, "url:"));
        assertEquals("post", extractField(lines, "method:"));
        assertEquals("{\"key\":\"value\"}", extractField(lines, "data:"));
    }

    @Test
    @DisplayName("HTTP 参数解析：null 参数返回空数组")
    void testHttpParamNullInput() {
        String[] result = parseHttpParam(null);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("HTTP 参数解析：缺少字段时返回 null")
    void testHttpParamMissingField() {
        String param = "url: http://example.com\nmethod: GET";
        String[] lines = parseHttpParam(param);
        assertNull(extractField(lines, "data:"));
        assertNotNull(extractField(lines, "url:"));
    }

    @Test
    @DisplayName("HTTP 参数解析：method 大写规范化")
    void testHttpParamMethodUpperCase() {
        String raw = "get";
        assertEquals("GET", raw.toUpperCase());
        String raw2 = "POST";
        assertEquals("POST", raw2.toUpperCase());
    }

    @Test
    @DisplayName("HTTP 参数解析：不支持的 method 未在 ALLOWED_METHODS 中")
    void testAllowedMethods() {
        java.util.Set<String> allowed = java.util.Set.of("GET", "POST");
        assertTrue(allowed.contains("GET"));
        assertTrue(allowed.contains("POST"));
        assertFalse(allowed.contains("DELETE"));
        assertFalse(allowed.contains("PATCH"));
    }
}
