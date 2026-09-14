package com.dalio.cloud.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FreeMarkerUtil 单元测试
 */
class FreeMarkerUtilTest {

    @Test
    @DisplayName("测试模板生成文本")
    void testGenerateString() {
        assertNull(FreeMarkerUtil.generateString(null, null));

        String template = "Hello, ${name}! Your balance is ${balance}, status: ${active}";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Antigravity");
        params.put("balance", 1234.56);
        params.put("active", true);

        String result = FreeMarkerUtil.generateString(template, params);
        assertNotNull(result);
        assertEquals("Hello, Antigravity! Your balance is 1234.56, status: true", result);

        // 重复调用以命中缓存
        String cachedResult = FreeMarkerUtil.generateString(template, params);
        assertEquals(result, cachedResult);
    }

    @Test
    @DisplayName("测试共享变量可用性")
    void testSharedVariables() {
        String template = "Empty: ${StrPool.EMPTY}, Dot: ${StrPool.DOT}";
        Map<String, Object> params = new HashMap<>();
        String result = FreeMarkerUtil.generateString(template, params);
        assertNotNull(result);
        assertEquals("Empty: , Dot: .", result);
    }
}
