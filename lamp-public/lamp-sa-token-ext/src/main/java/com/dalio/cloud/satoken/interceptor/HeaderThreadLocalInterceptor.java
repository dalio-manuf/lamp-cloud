package com.dalio.cloud.satoken.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import com.dalio.basic.boot.utils.WebUtils;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;

import java.util.Map;

/**
 * 拦截器：
 * 将请求头中的数据，封装到 ContextUtil
 *
 * <p>
 * 该拦截器在必须优先于系统中其他的业务拦截器。
 * <p>
 * 微服务模式，必须每个服务都启用该拦截器，通过 lamp.webmvc.header = true 启用
 * 单体模式 必须禁用该拦截器，通过 lamp.webmvc.header = false 禁用
 * <p>
 *
 * @author admin
 * @date 2020/10/31 9:49 下午
 */
@Slf4j
@RequiredArgsConstructor
public class HeaderThreadLocalInterceptor implements AsyncHandlerInterceptor {

    private static void clearDataScopeSafely() {
        try {
            Class<?> clazz = Class.forName("com.dalio.cloud.datascope.DataScopeHelper");
            clazz.getMethod("clearDataScope").invoke(null);
        } catch (ClassNotFoundException ignored) {
            // 未引入 lamp-data-scope-sdk 模块，无需清理
        } catch (Exception e) {
            log.trace("清理 DataScopeHelper 异常: {}", e.getMessage());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        ContextUtil.setLocale(WebUtils.getHeader(request, ContextConstants.LOCALE_HEADER));
        ContextUtil.setPath(WebUtils.getHeader(request, ContextConstants.PATH_HEADER));

        ContextUtil.setApplicationId(WebUtils.getHeader(request, ContextConstants.APPLICATION_ID_HEADER));
        ContextUtil.setClientId(WebUtils.getHeader(request, ContextConstants.CLIENT_ID_HEADER));
        ContextUtil.setLogTraceId(WebUtils.getHeader(request, ContextConstants.TRACE_ID_HEADER));
        ContextUtil.setGrayVersion(WebUtils.getHeader(request, ContextConstants.GRAY_VERSION));

        String userId = WebUtils.getHeader(request, ContextConstants.JWT_KEY_USER_ID);
        String employeeId = WebUtils.getHeader(request, ContextConstants.EMPLOYEE_ID_HEADER);
        if (userId != null) {
            MDC.put(ContextConstants.USER_ID_HEADER, userId);
        }
        if (employeeId != null) {
            MDC.put(ContextConstants.EMPLOYEE_ID_HEADER, employeeId);
        }
        ContextUtil.setUserId(userId);
        ContextUtil.setEmployeeId(employeeId);
        ContextUtil.setCurrentTopCompanyId(WebUtils.getHeader(request, ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER));
        ContextUtil.setCurrentCompanyId(WebUtils.getHeader(request, ContextConstants.CURRENT_COMPANY_ID_HEADER));
        ContextUtil.setCurrentDeptId(WebUtils.getHeader(request, ContextConstants.CURRENT_DEPT_ID_HEADER));

        Map<String, String> localMap = ContextUtil.getLocalMap();
        if (localMap != null) {
            localMap.forEach((k, v) -> {
                if (k != null && v != null) {
                    MDC.put(k, v);
                }
            });
        }
        log.debug("HeaderThreadLocalInterceptor url={}, method={}", request.getRequestURI(), request.getMethod());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextUtil.remove();
        MDC.clear();
        clearDataScopeSafely();
    }
}
