package com.dalio.cloud.common;

import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;
import com.dalio.cloud.common.cache.auth.TempAdminCacheKeyBuilder;
import com.dalio.cloud.common.cache.base.common.BaseDictCacheKeyBuilder;
import com.dalio.cloud.common.cache.base.system.RoleResourceCacheKeyBuilder;
import com.dalio.cloud.common.cache.common.*;
import com.dalio.cloud.common.cache.tenant.base.DefUserCacheKeyBuilder;
import com.dalio.cloud.common.cache.tenant.base.DictCacheKeyBuilder;
import com.dalio.cloud.common.cache.tenant.system.DefClientSecretCacheKeyBuilder;
import com.dalio.cloud.common.constant.*;
import com.dalio.cloud.common.properties.SystemProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        assertNotNull(TempAdminCacheKeyBuilder.builder("admin"));
        assertNotNull(TempAdminCacheKeyBuilder.builder("admin", "type"));
        assertNotNull(BaseDictCacheKeyBuilder.builder("dictKey"));
        assertNotNull(BaseDictCacheKeyBuilder.builder("dictKey", "field"));
        assertNotNull(RoleResourceCacheKeyBuilder.build(1L, 2L));
        assertNotNull(DefClientSecretCacheKeyBuilder.builder("cid", "secret"));
        assertNotNull(TodayLoginPvCacheKeyBuilder.build(java.time.LocalDate.now()));
        assertNotNull(TodayPvCacheKeyBuilder.build(java.time.LocalDate.now()));
        assertNotNull(TodayLoginIvCacheKeyBuilder.build(java.time.LocalDate.now()));
        assertNotNull(com.dalio.cloud.common.cache.tenant.base.DefUserEmailCacheKeyBuilder.builder("email@test.com"));
        assertNotNull(com.dalio.cloud.common.cache.tenant.base.DefUserIdCardCacheKeyBuilder.builder("110101199001011234"));
        assertNotNull(com.dalio.cloud.common.cache.tenant.base.DefUserUserNameCacheKeyBuilder.builder("admin"));
        assertNotNull(com.dalio.cloud.common.cache.tenant.base.DictParameterKeyBuilder.builder("KEY"));

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
            builder.getPattern();
            builder.getModular();
            builder.getField();
            builder.getValueType();
            builder.getExpire();
        }

        assertNotNull(com.dalio.cloud.common.cache.common.TotalLoginPvCacheKeyBuilder.build());
        assertNotNull(com.dalio.cloud.common.cache.common.TotalPvCacheKeyBuilder.build());
        assertNotNull(com.dalio.cloud.common.cache.common.TotalLoginIvCacheKeyBuilder.build());
        assertNotNull(com.dalio.cloud.common.cache.tenant.base.DefUserMobileCacheKeyBuilder.builder("13800000000"));
        assertNotNull(com.dalio.cloud.common.cache.auth.IsTenantAdminCacheKeyBuilder.builder(1L));
        assertNotNull(com.dalio.cloud.common.cache.base.system.RoleCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.system.DefClientCacheKeyBuilder.builder(1L));
        assertNotNull(com.dalio.cloud.common.cache.base.user.EmployeeRoleCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.base.user.EmployeeOrgCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.base.user.EmployeeCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.base.user.OrgRoleCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.application.ApplicationResourceCacheKeyBuilder.build(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.application.ResourceCacheKeyBuilder.builder(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.application.ResourceApiCacheKeyBuilder.builder(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.application.ResourceResourceApiCacheKeyBuilder.builder(1L));
        assertNotNull(com.dalio.cloud.common.cache.tenant.application.AllResourceApiCacheKeyBuilder.builder());
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

    @Test
    @DisplayName("测试 ServerApplication 启动与日志输出")
    void testServerApplication() {
        ServerApplication app = new ServerApplication();
        assertNotNull(app);

        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        when(context.getEnvironment()).thenReturn(env);
        when(env.getProperty("server.port", "8080")).thenReturn("9999");
        when(env.getProperty("spring.application.name")).thenReturn("lamp-test");
        when(env.getProperty("java.version")).thenReturn("17");
        when(env.getProperty("server.servlet.context-path", "")).thenReturn("/api");
        when(env.getProperty("spring.profiles.active")).thenReturn("dev");
        when(env.getProperty("LOG_PATH")).thenReturn("/tmp/logs");

        try (MockedStatic<SpringApplication> mockedSpring = mockStatic(SpringApplication.class)) {
            mockedSpring.when(() -> SpringApplication.run(eq(ServerApplication.class), any(String[].class)))
                    .thenReturn(context);
            ServerApplication.start(ServerApplication.class, new String[]{});
            mockedSpring.verify(() -> SpringApplication.run(eq(ServerApplication.class), any(String[].class)));
        }
    }

    @Test
    @DisplayName("测试 常量类与工具方法")
    void testConstantsAndAnnotations() {
        assertNotNull(AppendixType.System.DEF__USER__AVATAR);
        AppendixType.ALL_TYPES.add("CUSTOM_TYPE");
        AppendixType.assertType("CUSTOM_TYPE");
        assertThrows(RuntimeException.class, () -> AppendixType.assertType("UNKNOWN_TYPE"));

        assertNotNull(ParameterKey.LOGIN_POLICY);
        assertTrue(ParameterKey.LoginPolicy.ONLY_ONE_CLIENT.eq("ONLY_ONE_CLIENT"));
        assertTrue(ParameterKey.LoginPolicy.MANY.eq("MANY"));
        assertTrue(ParameterKey.LoginPolicy.ONLY_ONE.eq("ONLY_ONE"));
        assertFalse(ParameterKey.LoginPolicy.ONLY_ONE.eq("OTHER"));
        assertNotNull(BizMqQueue.TENANT_DS_FANOUT_EXCHANGE_OAUTH);
        assertNotNull(JobConstant.DEF_BASE_JOB_GROUP_NAME);
        assertNotNull(RoleConstant.TENANT_ADMIN);
        assertNotNull(BizConstant.INIT_DS_PARAM_METHOD);
        assertNotNull(SwaggerConstants.PARAM_TYPE_QUERY);
        assertNotNull(DefValConstants.DEF_TENANT_ID);
        assertNotNull(CacheKeyTable.System.DEF_USER);
        assertNotNull(CacheKeyModular.COMMON);
        assertNotNull(new CacheKeyModular() {
            @Override
            public String getTable() {
                return "test";
            }
        });
    }
}
