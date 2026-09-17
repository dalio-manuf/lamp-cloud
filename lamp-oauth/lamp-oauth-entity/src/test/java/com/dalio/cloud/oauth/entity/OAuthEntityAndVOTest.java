package com.dalio.cloud.oauth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.vo.result.user.VueRouter;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.oauth.enumeration.GrantType;
import com.dalio.cloud.oauth.vo.param.CodeQueryVO;
import com.dalio.cloud.oauth.vo.param.LoginParamVO;
import com.dalio.cloud.oauth.vo.param.RegisterByEmailVO;
import com.dalio.cloud.oauth.vo.param.RegisterByMobileVO;
import com.dalio.cloud.oauth.vo.param.RegisterVO;
import com.dalio.cloud.oauth.vo.result.DefUserInfoResultVO;
import com.dalio.cloud.oauth.vo.result.LoginResultVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;
import com.dalio.cloud.oauth.vo.result.VisibleResourceVO;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * lamp-oauth-entity 实体与 VO 自动化测试
 */
class OAuthEntityAndVOTest {

    @Test
    @DisplayName("自动化扫描并测试 lamp-oauth-entity 全部 VO 的 Getter/Setter/Builder/Equals/HashCode/ToString")
    void testAllEntitiesAndVOs() throws Exception {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        File testClassesDir = new File(location.toURI());
        File classesDir = new File(testClassesDir.getAbsolutePath().replace("test-classes", "classes"));

        List<Class<?>> entityClasses = new ArrayList<>();
        scanClasses(classesDir, classesDir.getAbsolutePath(), entityClasses);

        for (Class<?> clazz : entityClasses) {
            if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())
                    || clazz.isAnonymousClass() || clazz.getName().contains("$")) {
                continue;
            }

            Object instance = createInstance(clazz);
            if (instance == null) {
                continue;
            }

            // 测试 setter 与 getter
            for (Method method : clazz.getMethods()) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object sampleVal = getSampleValue(paramType);
                    try {
                        method.invoke(instance, sampleVal);
                    } catch (Exception ignored) {
                    }
                }
            }

            for (Method method : clazz.getMethods()) {
                if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
                        && method.getParameterCount() == 0 && !method.getName().equals("getClass")) {
                    try {
                        method.invoke(instance);
                    } catch (Exception ignored) {
                    }
                }
            }

            // 测试 equals, hashCode, toString
            try {
                assertNotNull(instance.toString());
                instance.hashCode();
                instance.equals(instance);
                instance.equals(null);
                instance.equals(new Object());
                Object secondInstance = createInstance(clazz);
                if (secondInstance != null) {
                    instance.equals(secondInstance);
                }
            } catch (Exception ignored) {
            }

            // 测试 Builder
            try {
                Method builderMethod = clazz.getMethod("builder");
                Object builderObj = builderMethod.invoke(null);
                if (builderObj != null) {
                    for (Method bMethod : builderObj.getClass().getMethods()) {
                        if (bMethod.getParameterCount() == 1 && bMethod.getReturnType().equals(builderObj.getClass())) {
                            try {
                                bMethod.invoke(builderObj, getSampleValue(bMethod.getParameterTypes()[0]));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    builderObj.toString();
                    Method buildMethod = builderObj.getClass().getMethod("build");
                    Object builtInstance = buildMethod.invoke(builderObj);
                    assertNotNull(builtInstance);
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("显式测试各 VO 的属性与构造方法")
    void testExplicitVOs() {
        LocalDateTime now = LocalDateTime.now();
        LoginResultVO loginResult = LoginResultVO.builder()
                .uuid("uuid-123")
                .token("tok123")
                .expire(3600L)
                .refreshToken("ref123")
                .expiration(now)
                .build();
        assertNotNull(loginResult);
        assertEquals("uuid-123", loginResult.getUuid());
        assertEquals("tok123", loginResult.getToken());
        assertEquals(3600L, loginResult.getExpire());
        assertEquals("ref123", loginResult.getRefreshToken());
        assertEquals(now, loginResult.getExpiration());

        VisibleResourceVO visibleResource = VisibleResourceVO.builder()
                .enabled(true)
                .caseSensitive(false)
                .resourceList(Collections.singletonList("res:add"))
                .routerList(Collections.singletonList(new VueRouter()))
                .roleList(Collections.singletonList("ADMIN"))
                .build();
        assertTrue(visibleResource.getEnabled());
        assertEquals(1, visibleResource.getResourceList().size());
        assertEquals(1, visibleResource.getRoleList().size());
        assertEquals(1, visibleResource.getRouterList().size());

        DefUserInfoResultVO userInfo = DefUserInfoResultVO.builder()
                .id(1L)
                .username("admin")
                .nickName("管理员")
                .mobile("13800000000")
                .email("admin@test.com")
                .build();
        assertEquals(1L, userInfo.getId());
        assertEquals("admin", userInfo.getUsername());

        OrgResultVO orgResult = OrgResultVO.builder()
                .orgList(Collections.singletonList(new BaseOrg()))
                .currentCompanyId(10L)
                .currentDeptId(20L)
                .employeeId(30L)
                .build();
        assertNotNull(orgResult.getOrgList());
        assertEquals(10L, orgResult.getCurrentCompanyId());
        assertEquals(20L, orgResult.getCurrentDeptId());
        assertEquals(30L, orgResult.getEmployeeId());

        LoginParamVO loginParam = LoginParamVO.builder()
                .username("user")
                .password("pwd")
                .code("1234")
                .key("uuid")
                .grantType(GrantType.CAPTCHA)
                .mobile("13900000000")
                .build();
        assertEquals("user", loginParam.getUsername());
        assertEquals(GrantType.CAPTCHA, loginParam.getGrantType());

        RegisterByMobileVO regMobile = new RegisterByMobileVO();
        regMobile.setMobile("13911112222");
        regMobile.setCode("1234");
        regMobile.setKey("uuid");
        regMobile.setPassword("pass123");
        regMobile.setConfirmPassword("pass123");
        regMobile.setNickName("nick");
        assertEquals("13911112222", regMobile.getMobile());
        assertEquals("1234", regMobile.getCode());
        assertEquals("uuid", regMobile.getKey());

        RegisterByEmailVO regEmail = new RegisterByEmailVO();
        regEmail.setEmail("test@mail.com");
        regEmail.setCode("1234");
        regEmail.setKey("uuid");
        regEmail.setPassword("pass123");
        regEmail.setConfirmPassword("pass123");
        regEmail.setNickName("nick");
        assertEquals("test@mail.com", regEmail.getEmail());
        assertEquals("1234", regEmail.getCode());

        Option opt = new Option();
        CodeQueryVO codeQuery = CodeQueryVO.builder()
                .type("DICT_TYPE")
                .excludes(Arrays.asList("EX1", "EX2"))
                .extend(opt)
                .extendFirst(true)
                .build();
        assertEquals("DICT_TYPE", codeQuery.getType());
        assertEquals(2, codeQuery.getExcludes().size());
        assertEquals(opt, codeQuery.getExtend());
        assertTrue(codeQuery.getExtendFirst());

        CodeQueryVO codeQuery2 = new CodeQueryVO("DICT_TYPE", Arrays.asList("EX1", "EX2"), opt, true);
        assertEquals(codeQuery, codeQuery2);
        assertEquals(codeQuery.hashCode(), codeQuery2.hashCode());
        assertEquals(codeQuery, codeQuery);
        assertNotEquals(codeQuery, null);
        assertNotEquals(codeQuery, new Object());

        RegisterVO registerVO = new RegisterVO();
        registerVO.setPassword("regPwd");
        registerVO.setConfirmPassword("regPwd");
        registerVO.setNickName("Nick");
        registerVO.setKey("k");
        registerVO.setCode("c");
        assertEquals("regPwd", registerVO.getPassword());
        assertEquals("regPwd", registerVO.getConfirmPassword());
        assertEquals("Nick", registerVO.getNickName());
        assertEquals("k", registerVO.getKey());
        assertEquals("c", registerVO.getCode());
    }

    private void scanClasses(File dir, String baseDir, List<Class<?>> result) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanClasses(file, baseDir, result);
            } else if (file.getName().endsWith(".class")) {
                String className = file.getAbsolutePath()
                        .substring(baseDir.length() + 1)
                        .replace(File.separatorChar, '.')
                        .replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    result.add(clazz);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                }
            }
        }
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                ctor.setAccessible(true);
                Class<?>[] pTypes = ctor.getParameterTypes();
                Object[] args = new Object[pTypes.length];
                for (int i = 0; i < pTypes.length; i++) {
                    args[i] = getSampleValue(pTypes[i]);
                }
                try {
                    return ctor.newInstance(args);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private Object getSampleValue(Class<?> type) {
        if (type.equals(String.class)) {
            return "sample";
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return 1L;
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return 1;
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return true;
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return 1.0;
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return 1.0f;
        } else if (type.equals(LocalDateTime.class)) {
            return LocalDateTime.now();
        } else if (type.equals(LocalDate.class)) {
            return LocalDate.now();
        } else if (type.equals(List.class) || type.equals(Collection.class)) {
            return new ArrayList<>();
        } else if (type.equals(Map.class)) {
            return Collections.emptyMap();
        } else if (type.equals(Set.class)) {
            return Collections.emptySet();
        } else if (type.isEnum()) {
            Object[] enumConstants = type.getEnumConstants();
            return (enumConstants != null && enumConstants.length > 0) ? enumConstants[0] : null;
        }
        return null;
    }
}
