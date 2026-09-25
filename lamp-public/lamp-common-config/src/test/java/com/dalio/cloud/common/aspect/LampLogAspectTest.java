package com.dalio.cloud.common.aspect;

import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.common.properties.SystemProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LampLogAspectTest {

    private LampLogAspect aspect;
    private SystemProperties systemProperties;

    @BeforeEach
    void setUp() {
        systemProperties = new SystemProperties();
        systemProperties.setRecordLampArgs(true);
        systemProperties.setRecordLampResult(true);
        aspect = new LampLogAspect(systemProperties);
        ContextUtil.remove();
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试切入点定义方法")
    void testPointcut() {
        assertDoesNotThrow(() -> aspect.lampLogAspect());
    }

    @Test
    @DisplayName("测试正常调用环绕通知")
    void testInvokeSuccess() throws Throwable {
        ContextUtil.setLogTraceId("trace-123");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(signature.getDeclaringType()).thenReturn(LampLogAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");
        when(signature.getParameterTypes()).thenReturn(new Class<?>[]{String.class, Integer.class});
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{"arg1", 100});
        when(pjp.proceed()).thenReturn("successResult");

        Object result = aspect.invoke(pjp);
        assertEquals("successResult", result);
    }

    @Test
    @DisplayName("测试无TraceId且关闭参数日志时环绕通知")
    void testInvokeWithoutTraceIdAndNoArgs() throws Throwable {
        systemProperties.setRecordLampArgs(false);
        systemProperties.setRecordLampResult(false);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(signature.getDeclaringType()).thenReturn(LampLogAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");
        when(signature.getParameterTypes()).thenReturn(new Class<?>[]{});
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenReturn(null);

        Object result = aspect.invoke(pjp);
        assertEquals(null, result);
    }

    @Test
    @DisplayName("测试非MethodSignature签名与空类型")
    void testInvokeNonMethodSignature() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(signature.getDeclaringType()).thenReturn(LampLogAspectTest.class);
        when(signature.getName()).thenReturn("otherSignature");
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenReturn("done");

        Object result = aspect.invoke(pjp);
        assertEquals("done", result);
    }

    @Test
    @DisplayName("测试被拦截方法抛出异常分支")
    void testInvokeWithException() throws Throwable {
        ContextUtil.setLogTraceId("trace-error");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);

        when(signature.getDeclaringType()).thenReturn(LampLogAspectTest.class);
        when(signature.getName()).thenReturn("errorMethod");
        when(signature.getParameterTypes()).thenReturn(null);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenThrow(new IllegalArgumentException("test exception"));

        assertThrows(IllegalArgumentException.class, () -> aspect.invoke(pjp));
    }
}
