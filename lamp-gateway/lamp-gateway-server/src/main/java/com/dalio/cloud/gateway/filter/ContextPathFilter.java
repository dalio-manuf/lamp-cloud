package com.dalio.cloud.gateway.filter;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * ContextPath 转发兼容过滤器
 * 用于兼容历史 /api 路径前缀访问，非必要场景直接放行以消除无谓对象创建开销
 *
 * @author dalio
 * @date 2019/07/31
 */
@Component
@Order(-1100)
@RequiredArgsConstructor
public class ContextPathFilter implements WebFilter {
    private final ServerProperties serverProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String contextPath = serverProperties.getServlet() != null ? serverProperties.getServlet().getContextPath() : null;
        if (StrUtil.isBlank(contextPath)) {
            return chain.filter(exchange);
        }

        String requestPath = exchange.getRequest().getPath().pathWithinApplication().value();
        if (!requestPath.startsWith(contextPath)) {
            return chain.filter(exchange);
        }

        String newPath = requestPath.substring(contextPath.length());
        return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path(newPath).build()).build());
    }
}

