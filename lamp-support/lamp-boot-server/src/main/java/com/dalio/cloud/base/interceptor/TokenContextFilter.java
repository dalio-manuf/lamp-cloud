package com.dalio.cloud.base.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.common.utils.Base64Util;

import static com.dalio.basic.context.ContextConstants.APPLICATION_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.APPLICATION_ID_KEY;
import static com.dalio.basic.context.ContextConstants.CLIENT_KEY;

/**
 * 用户信息解析器 一定要在AuthenticationFilter之前执行
 *
 * @author admin
 * @version v1.0
 * @date 2021/12/28 2:36 下午
 * @create [2021/12/28 2:36 下午 ] [admin] [初始创建]
 */
@Slf4j
@RequiredArgsConstructor
public class TokenContextFilter implements AsyncHandlerInterceptor {
    private final String profiles;
    private final IgnoreProperties ignoreProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return true;
        }
        ContextUtil.setBoot(true);
        ContextUtil.setPath(getHeader(ContextConstants.PATH_HEADER, request));
        String traceId = IdUtil.fastSimpleUUID();
        MDC.put(ContextConstants.TRACE_ID_HEADER, traceId);
        try {
            // 1,解码 Authorization
            parseClient(request);

            // 2, 获取 应用id
            parseApplication(request);


        } catch (Exception e) {
            log.error("request={}", request.getRequestURL(), e);
            throw e;
        }

        return true;
    }


    private void parseClient(HttpServletRequest request) {
        try {
            String base64Authorization = getHeader(CLIENT_KEY, request);
            if (StrUtil.isNotEmpty(base64Authorization)) {
                String[] client = Base64Util.getClient(base64Authorization);
                ContextUtil.setClientId(client[0]);
            }
        } catch (Exception ignored) {
        }
    }

    private void parseApplication(HttpServletRequest request) {
        String applicationIdStr = getHeader(APPLICATION_ID_KEY, request);
        if (StrUtil.isNotEmpty(applicationIdStr)) {
            ContextUtil.setApplicationId(applicationIdStr);
            MDC.put(APPLICATION_ID_HEADER, applicationIdStr);
        }
    }


    private String getHeader(String name, HttpServletRequest request) {
        String value = request.getHeader(name);
        if (StrUtil.isEmpty(value)) {
            value = request.getParameter(name);
        }
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        return URLUtil.decode(value);
    }


    protected boolean isDev(String token) {
        return !StrPool.PROD.equalsIgnoreCase(profiles) && (StrPool.TEST_TOKEN.equalsIgnoreCase(token) || StrPool.TEST.equalsIgnoreCase(token));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextUtil.remove();
        MDC.clear();
        clearDataScopeSafely();
    }

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
}
