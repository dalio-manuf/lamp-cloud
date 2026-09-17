package com.dalio.cloud.oauth.enumeration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GrantType 枚举单元测试
 */
class OAuthEnumTest {

    @Test
    @DisplayName("测试 GrantType 枚举的基本方法与匹配逻辑")
    void testGrantType() {
        for (GrantType item : GrantType.values()) {
            assertNotNull(item.getCode());
            assertNotNull(item.getDesc());
            assertEquals(item, GrantType.valueOf(item.name()));
            assertEquals(item, GrantType.get(item.name()));
            assertEquals(item, GrantType.match(item.name().toLowerCase(), null));
            assertTrue(item.eq(item));
        }

        // 测试不匹配与缺省回退
        assertNull(GrantType.get("NON_EXISTING"));
        assertNull(GrantType.match("NON_EXISTING", null));
        assertEquals(GrantType.CAPTCHA, GrantType.match("NON_EXISTING", GrantType.CAPTCHA));

        // 测试 eq 方法
        assertTrue(GrantType.PASSWORD.eq(GrantType.PASSWORD));
        assertFalse(GrantType.PASSWORD.eq(GrantType.MOBILE));
        assertFalse(GrantType.PASSWORD.eq((GrantType) null));
        assertFalse(GrantType.PASSWORD.eq((String) null));
        assertTrue(GrantType.PASSWORD.eq("PASSWORD"));

        // 测试 getCode 与 getDesc
        assertEquals("CAPTCHA", GrantType.CAPTCHA.getCode());
        assertEquals("验证码登录", GrantType.CAPTCHA.getDesc());
        assertEquals("PASSWORD", GrantType.PASSWORD.getCode());
        assertEquals("账号密码登录", GrantType.PASSWORD.getDesc());
        assertEquals("MOBILE", GrantType.MOBILE.getCode());
        assertEquals("手机登录", GrantType.MOBILE.getDesc());
    }
}
