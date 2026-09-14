package com.dalio.cloud.common.utils;

import com.dalio.basic.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base64Util 单元测试
 */
class Base64UtilTest {

    @Test
    @DisplayName("测试正常解析 Basic 认证头")
    void testGetClientSuccess() {
        String clientId = "myClientId";
        String clientSecret = "mySecret123";
        String raw = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));

        // 包含 Basic 前缀
        String[] client1 = Base64Util.getClient("Basic " + encoded);
        assertNotNull(client1);
        assertEquals(2, client1.length);
        assertEquals(clientId, client1[0]);
        assertEquals(clientSecret, client1[1]);

        // 小写 basic 前缀
        String[] client2 = Base64Util.getClient("basic " + encoded);
        assertNotNull(client2);
        assertEquals(clientId, client2[0]);
        assertEquals(clientSecret, client2[1]);

        // 不含 Basic 前缀
        String[] client3 = Base64Util.extractClient(encoded);
        assertNotNull(client3);
        assertEquals(clientId, client3[0]);
        assertEquals(clientSecret, client3[1]);
    }

    @Test
    @DisplayName("测试客户端参数未传递时的异常")
    void testClientEmptyThrowsException() {
        assertThrows(BizException.class, () -> Base64Util.getClient(null));
        assertThrows(BizException.class, () -> Base64Util.getClient(""));
        assertThrows(BizException.class, () -> Base64Util.getClient("   "));
        assertThrows(BizException.class, () -> Base64Util.extractClient(null));
        assertThrows(BizException.class, () -> Base64Util.extractClient(""));
    }

    @Test
    @DisplayName("测试不包含冒号分隔符的异常")
    void testClientWithoutColonThrowsException() {
        String encoded = Base64.getEncoder().encodeToString("onlyClientId".getBytes(StandardCharsets.UTF_8));
        assertThrows(BizException.class, () -> Base64Util.getClient("Basic " + encoded));
    }

    @Test
    @DisplayName("测试 base64Decoder 处理空值与正常解码")
    void testBase64Decoder() {
        assertEquals("", Base64Util.base64Decoder(null));
        assertEquals("", Base64Util.base64Decoder(""));

        String original = "Hello, lamp-cloud!";
        String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
        assertEquals(original, Base64Util.base64Decoder(encoded));
    }
}
