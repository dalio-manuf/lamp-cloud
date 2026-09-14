package com.dalio.cloud.gateway.fallback;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.dalio.basic.base.R;
import com.dalio.basic.exception.code.ExceptionCode;

/**
 * 响应超时熔断降级处理器
 *
 * @author dalio
 */
@RestController
@Slf4j
@Tag(name = "网关熔断降级回调")
public class FallbackController {

    @Operation(summary = "服务熔断降级统一响应")
    @RequestMapping("/fallback")
    public Mono<R<String>> fallback(ServerWebExchange exchange) {
        log.warn("网关触发微服务超时熔断降级: uri={}", exchange.getRequest().getURI());
        return Mono.just(R.validFail(ExceptionCode.SYSTEM_TIMEOUT));
    }
}

