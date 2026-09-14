package com.dalio.cloud.gateway.filter;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.LinkedHashSet;

import static org.springframework.cloud.gateway.filter.headers.XForwardedHeadersFilter.X_FORWARDED_PREFIX_HEADER;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Swagger 转发 Header 增强过滤器
 * 重写并补充 XForwardedHeadersFilter 所需的 contextPath 前缀配置
 *
 * @author dalio
 * @date 2019/08/13
 */
@Component
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class MySwaggerXForwardedHeadersFilter implements HttpHeadersFilter, Ordered {
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Override
    public int getOrder() {
        return OrderedConstant.SWAGGER;
    }

    @Override
    public boolean supports(Type type) {
        return true;
    }

    @Override
    public HttpHeaders filter(HttpHeaders input, ServerWebExchange exchange) {
        HttpHeaders updated = new HttpHeaders();
        updated.addAll(input);

        LinkedHashSet<URI> originalUris = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        URI requestUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);

        if (originalUris != null && requestUri != null) {
            originalUris.forEach(originalUri -> {
                if (originalUri != null && originalUri.getPath() != null) {
                    String originalUriPath = stripTrailingSlash(originalUri);
                    String requestUriPath = stripTrailingSlash(requestUri);

                    if (StrUtil.isNotEmpty(requestUriPath) && originalUriPath.endsWith(requestUriPath)) {
                        String prefix = originalUriPath.substring(0, originalUriPath.length() - requestUriPath.length());
                        if (StrUtil.isNotEmpty(prefix)) {
                            updated.set(X_FORWARDED_PREFIX_HEADER, contextPath + prefix);
                        }
                    }
                }
            });
        }

        return updated;
    }

    private String stripTrailingSlash(URI uri) {
        if (uri == null || uri.getPath() == null) {
            return "";
        }
        String path = uri.getPath();
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}

