package com.dalio.cloud.common.aspect;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.common.properties.SystemProperties;

/**
 * 操作日志使用spring event异步入库
 *
 * @author admin
 * @date 2019-07-01 15:15
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class LampLogAspect extends BaseLogAspect {
    private final SystemProperties systemProperties;

    /***
     * 定义controller切入点拦截规则：拦截标记SysLog注解和指定包下的方法
     * 2个表达式加起来才能拦截所有Controller 或者继承了BaseController的方法
     *
     * execution(public * com.dalio.cloud.*.*(..)) 解释：
     * 第一个* 任意返回类型
     * 第二个* com.dalio.cloud 包下的所有类
     * 第三个* 类下的所有方法
     * ()中间的.. 任意参数
     *
     */
    @Pointcut("execution(* com.dalio.cloud..controller..*.*(..)) || execution(* com.dalio.cloud..service..*.*(..)) " +
            "|| execution(* com.dalio.cloud..biz..*.*(..))")
    public void lampLogAspect() {

    }

    @Around("lampLogAspect()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        String logTraceId = ContextUtil.getLogTraceId() == null ? StrPool.EMPTY : ContextUtil.getLogTraceId();
        String types = getParamTypes(joinPoint);

        outArgsLog(joinPoint, logTraceId, types, joinPoint.getArgs(), systemProperties.getRecordLampArgs());
        long start = System.currentTimeMillis();
        try {
            Object retVal = joinPoint.proceed();
            outResultLog(joinPoint, logTraceId, types, start, retVal, systemProperties.getRecordLampResult());
            return retVal;
        } catch (Throwable e) {
            log.error("<<<< [traceId:{}] {}.{}({}) end... {} ms", logTraceId, joinPoint.getSignature().getDeclaringType(),
                    joinPoint.getSignature().getName(), types, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

}
