package com.dalio.cloud.common;

import com.dalio.basic.model.cache.CacheHashKey;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.common.cache.common.CaptchaCacheKeyBuilder;
import com.dalio.cloud.common.cache.common.TokenUserIdCacheKeyBuilder;
import com.dalio.cloud.common.cache.tenant.base.DefUserCacheKeyBuilder;
import com.dalio.cloud.common.cache.tenant.base.DictCacheKeyBuilder;
import com.dalio.cloud.common.properties.SystemProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CacheKeyBuilder 与 SystemProperties 单元测试
 */
class CommonCacheAndPropertiesTest {

    @Test
    @DisplayName("测试各类 CacheKeyBuilder 构建与属性")
    void testCacheKeyBuilders() {
        assertNotNull(DictCacheKeyBuilder.builder("GLOBAL_STATUS"));
        assertNotNull(DictCacheKeyBuilder.builder("GLOBAL_STATUS", "ENABLE"));
        assertNotNull(CaptchaCacheKeyBuilder.build("uuid123", "template"));
        assertNotNull(TokenUserIdCacheKeyBuilder.builder("token_xyz"));
        assertNotNull(DefUserCacheKeyBuilder.builder(1001L));

        com.dalio.basic.model.cache.CacheKeyBuilder[] builders = new com.dalio.basic.model.cache.CacheKeyBuilder[]{
                new com.dalio.cloud.common.cache.tenant.base.DefUserEmailCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.base.DefUserIdCardCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.base.DefUserMobileCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.base.DefUserUserNameCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.base.DictParameterKeyBuilder(),
                new com.dalio.cloud.common.cache.auth.IsTenantAdminCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.auth.TempAdminCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.system.RoleCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.system.RoleResourceCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.common.BaseDictCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.EmployeeCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.EmployeeOrgCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.EmployeeRoleCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.OrgCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.OrgRoleCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.base.user.PositionCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.LoginLogBrowserCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.LoginLogSystemCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.LoginLogTenDayCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.OnlineCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.ParameterKeyCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TodayLoginIvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TodayLoginPvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TodayPvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TotalLoginIvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TotalLoginPvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.common.TotalPvCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.system.DefClientCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.system.DefClientSecretCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.AllResourceApiCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.ApplicationCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.ApplicationResourceCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.ResourceApiCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.ResourceCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.tenant.application.ResourceResourceApiCacheKeyBuilder(),
                new com.dalio.cloud.common.cache.VerificationCodeCacheKeyBuilder()
        };

        for (com.dalio.basic.model.cache.CacheKeyBuilder builder : builders) {
            assertNotNull(builder.getTable());
            assertNotNull(builder.key("testKey"));
        }
    }

    @Test
    @DisplayName("测试 SystemProperties 属性读写")
    void testSystemProperties() {
        SystemProperties properties = new SystemProperties();
        assertTrue(properties.getVerifyPassword());
        assertTrue(properties.getVerifyCaptcha());
        assertEquals(10, properties.getMaxPasswordErrorNum());
        assertEquals("123456", properties.getDefPwd());
        assertEquals("0", properties.getPasswordErrorLockUserTime());
        assertFalse(properties.getRecordLamp());
        assertTrue(properties.getRecordLampArgs());
        assertTrue(properties.getRecordLampResult());
        assertFalse(properties.getNotAllowWrite());

        properties.setVerifyPassword(false);
        assertFalse(properties.getVerifyPassword());

        properties.setDefPwd("newPassword");
        assertEquals("newPassword", properties.getDefPwd());

        properties.setCachePrefix("customPrefix");
        assertEquals("customPrefix", properties.getCachePrefix());

        properties.setEnumPackage("com.dalio.cloud.model.enumeration");
        assertEquals("com.dalio.cloud.model.enumeration", properties.getEnumPackage());

        Map<String, List<String>> notAllow = new HashMap<>();
        notAllow.put("DEMO", Collections.singletonList("POST"));
        properties.setNotAllowWriteList(notAllow);
        assertEquals(1, properties.getNotAllowWriteList().size());
    }
}
