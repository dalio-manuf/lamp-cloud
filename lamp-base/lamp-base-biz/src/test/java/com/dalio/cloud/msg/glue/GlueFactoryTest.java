package com.dalio.cloud.msg.glue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlueFactoryTest {

    @Test
    @DisplayName("测试正常 Groovy 脚本安全执行")
    void testExeGroovyScriptSuccess() {
        GlueFactory factory = new GlueFactory();
        String script = "return a + b;";
        Map<String, Object> params = Map.of("a", 10, "b", 25);

        Object result = factory.exeGroovyScript(script, params);
        assertEquals(35, result);
    }

    @Test
    @DisplayName("测试空脚本异常")
    void testEmptyScriptThrows() {
        GlueFactory factory = new GlueFactory();
        assertThrows(IllegalArgumentException.class, () -> {
            factory.exeGroovyScript(null, Collections.emptyMap());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            factory.exeGroovyScript("   ", Collections.emptyMap());
        });
    }

    @Test
    @DisplayName("测试安全沙箱拦截黑名单危险 Token (Runtime/ProcessBuilder/File/ClassLoader)")
    void testSecurityBlacklistTokens() {
        GlueFactory factory = new GlueFactory();

        // 1. Runtime.getRuntime().exec
        String evilScript1 = "Runtime.getRuntime().exec('ls');";
        assertThrows(SecurityException.class, () -> factory.exeGroovyScript(evilScript1, Collections.emptyMap()));

        // 2. ProcessBuilder
        String evilScript2 = "new ProcessBuilder('whoami').start();";
        assertThrows(SecurityException.class, () -> factory.exeGroovyScript(evilScript2, Collections.emptyMap()));

        // 3. File 操作
        String evilScript3 = "new File('/etc/passwd').text;";
        assertThrows(SecurityException.class, () -> factory.exeGroovyScript(evilScript3, Collections.emptyMap()));

        // 4. 反射 Class.forName
        String evilScript4 = "Class.forName('java.lang.System');";
        assertThrows(SecurityException.class, () -> factory.exeGroovyScript(evilScript4, Collections.emptyMap()));

        // 5. 类加载 ClassLoader
        String evilScript5 = "ClassLoader cl = this.getClass().getClassLoader();";
        assertThrows(SecurityException.class, () -> factory.exeGroovyScript(evilScript5, Collections.emptyMap()));
    }

    @Test
    @DisplayName("测试 loadNewInstance 异常情况")
    void testLoadNewInstanceErrors() {
        GlueFactory factory = new GlueFactory();
        // 空脚本
        assertThrows(RuntimeException.class, () -> factory.loadNewInstance(""));

        // 不是 MsgStrategy 实例
        String notMsgStrategy = "class DummyBean {} return new DummyBean();";
        assertThrows(RuntimeException.class, () -> factory.loadNewInstance(notMsgStrategy));
    }
}
