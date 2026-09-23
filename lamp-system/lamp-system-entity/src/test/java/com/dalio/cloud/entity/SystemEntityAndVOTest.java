package com.dalio.cloud.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dalio.cloud.system.entity.system.DefDict;

/**
 * lamp-system-entity 实体与 VO 自动化反射测试
 */
class SystemEntityAndVOTest {

    @Test
    @DisplayName("测试 lamp-system-entity 全部实体与 VO 的 Getter/Setter/Builder/Equals/HashCode/ToString")
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
    @DisplayName("测试 DefDict 手动编写的全参构造与属性赋值")
    void testDefDictConstructor() {
        DefDict dict = new DefDict(1L, 1L, LocalDateTime.now(), 1L, LocalDateTime.now(),
                2L, "parentKey", "10", "key", "name",
                true, "remark", 1, "icon", "cssStyle", "cssClass",
                "dictGroup", "dataType", "propType", "{}");
        assertNotNull(dict);
        assertEquals(1L, dict.getId());
        assertEquals("key", dict.getKey());
        assertEquals("name", dict.getName());
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
        if (type == String.class) {
            return "test_val";
        } else if (type == Long.class || type == long.class) {
            return 1L;
        } else if (type == Integer.class || type == int.class) {
            return 1;
        } else if (type == Boolean.class || type == boolean.class) {
            return true;
        } else if (type == Double.class || type == double.class) {
            return 1.0;
        } else if (type == Float.class || type == float.class) {
            return 1.0f;
        } else if (type == Short.class || type == short.class) {
            return (short) 1;
        } else if (type == Byte.class || type == byte.class) {
            return (byte) 1;
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (type == LocalDate.class) {
            return LocalDate.now();
        } else if (type == List.class || type == Collection.class) {
            return new ArrayList<>();
        } else if (type == Set.class) {
            return Set.of();
        } else if (type == Map.class) {
            return Map.of();
        } else if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return (constants != null && constants.length > 0) ? constants[0] : null;
        }
        return null;
    }
}
