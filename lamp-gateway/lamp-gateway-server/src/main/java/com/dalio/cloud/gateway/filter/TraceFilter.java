package com.dalio.cloud.gateway.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.dalio.basic.context.ContextConstants;

/**
 * 日志链路追踪 ID 过滤器
 * 优先提取已有的链路跟踪 ID，不存在时自动生成并透传至下游服务 Header 与 MDC
 *
 * @author dalio
 * @date 2020年03月09日18:02:47
 */
@Component
public class TraceFilter implements WebFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(ContextConstants.TRACE_ID_HEADER);
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.fastSimpleUUID();
        }

        MDC.put(ContextConstants.TRACE_ID_HEADER, traceId);
        String finalTraceId = traceId;
        ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate()
                .headers(h -> h.set(ContextConstants.TRACE_ID_HEADER, finalTraceId))
                .build();
        return chain.filter(exchange.mutate().request(serverHttpRequest).build())
                .doFinally(signalType -> MDC.remove(ContextConstants.TRACE_ID_HEADER));
    }

    @Override
    public int getOrder() {
        return OrderedConstant.TRACE;
    }
}

