package com.dalio.cloud.gateway.fallback;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.dalio.basic.base.R;
import com.dalio.basic.exception.code.ExceptionCode;

/**
 * 响应超时熔断处理器
 *
 * @author admin
 */
@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<R> fallback(ServerWebExchange exchange) {
        return Mono.just(R.validFail(ExceptionCode.SYSTEM_TIMEOUT));
    }
}
