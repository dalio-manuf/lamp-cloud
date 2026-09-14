package com.dalio.cloud.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author admin
 * @version v1.0
 * @date 2021/9/4 8:34 下午
 * @create [2021/9/4 8:34 下午 ] [admin] [初始创建]
 */
@Slf4j
public abstract class BaseLogAspect {

    protected String getParamTypes(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature ms) {
            Class<?>[] parameterTypes = ms.getParameterTypes();
            if (parameterTypes == null || parameterTypes.length == 0) {
                return "";
            }
            return Arrays.stream(parameterTypes)
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }


    protected void outArgsLog(ProceedingJoinPoint joinPoint, String logTraceId, String types, Object[] args, boolean flag) {
        log.info(">>>> [traceId:{}] {}.{}({}) start...", logTraceId, joinPoint.getSignature().getDeclaringType(),
                joinPoint.getSignature().getName(), types);
        if (flag) {
            log.info(">>>> [traceId:{}] args={}", logTraceId, args);
        }
    }

    protected void outResultLog(ProceedingJoinPoint joinPoint, String logTraceId, String types, long start, Object retVal, boolean flag) {
        log.info("<<<< [traceId:{}] {}.{}({}) end... {} ms", logTraceId, joinPoint.getSignature().getDeclaringType(),
                joinPoint.getSignature().getName(), types, System.currentTimeMillis() - start);
        if (flag) {
            log.info("<<<< [traceId:{}] result={}", logTraceId, retVal);
        }
    }
}
