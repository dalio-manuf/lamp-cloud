package com.dalio.cloud.gateway.config;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.base.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关全局 JSON 异常处理器
 *
 * @author dalio
 * @date 2021/10/29 14:08
 */
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonExceptionHandler.class);
    private static final String EXCEPTION_HANDLER_RESULT_ATTR = "lamp.gateway.exceptionHandlerResult";
    /**
     * MessageReader
     */
    private List<HttpMessageReader<?>> messageReaders = Collections.emptyList();
    /**
     * MessageWriter
     */
    private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();
    /**
     * ViewResolvers
     */
    private List<ViewResolver> viewResolvers = Collections.emptyList();

    /**
     * 参考 AbstractErrorWebExceptionHandler
     */
    public void setMessageReaders(List<HttpMessageReader<?>> messageReaders) {
        Assert.notNull(messageReaders, "'messageReaders' must not be null");
        this.messageReaders = messageReaders;
    }

    /**
     * 参考 AbstractErrorWebExceptionHandler
     */
    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    /**
     * 参考 AbstractErrorWebExceptionHandler
     */
    public void setMessageWriters(List<HttpMessageWriter<?>> messageWriters) {
        Assert.notNull(messageWriters, "'messageWriters' must not be null");
        this.messageWriters = messageWriters;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 按照异常类型进行处理
        HttpStatusCode httpStatus;
        String body;
        if (ex instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
            body = "Service Not Found";
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            httpStatus = responseStatusException.getStatusCode();
            body = responseStatusException.getMessage();
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            body = StrUtil.isNotEmpty(ex.getMessage()) ? ex.getMessage() : "Internal Server Error";
        }
        // 封装响应体
        Map<String, Object> result = new HashMap<>(4);
        result.put("httpStatus", httpStatus);

        ServerHttpRequest request = exchange.getRequest();
        String msg = R.result(httpStatus.value(), null, body, body).setPath(request.getPath().toString()).toString();
        result.put("body", msg);

        // 记录完整错误堆栈日志
        log.error("[全局异常处理] 异常请求路径: {}, 异常信息: ", request.getPath(), ex);

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        exchange.getAttributes().put(EXCEPTION_HANDLER_RESULT_ATTR, result);
        ServerRequest newRequest = ServerRequest.create(exchange, this.messageReaders);
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse).route(newRequest)
                .switchIfEmpty(Mono.error(ex))
                .flatMap(handler -> handler.handle(newRequest))
                .flatMap(response -> write(exchange, response));
    }

    /**
     * 参考 DefaultErrorWebExceptionHandler
     */
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) request.attribute(EXCEPTION_HANDLER_RESULT_ATTR).orElse(Collections.emptyMap());
        HttpStatusCode status = result.get("httpStatus") instanceof HttpStatusCode statusCode ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR;
        String body = result.get("body") != null ? result.get("body").toString() : "Internal Server Error";
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body));
    }

    /**
     * 参考 AbstractErrorWebExceptionHandler
     */
    private Mono<? extends Void> write(ServerWebExchange exchange,
                                       ServerResponse response) {
        exchange.getResponse().getHeaders()
                .setContentType(response.headers().getContentType());
        return response.writeTo(exchange, new ResponseContext());
    }

    /**
     * 参考 AbstractErrorWebExceptionHandler
     */
    private class ResponseContext implements ServerResponse.Context {

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return JsonExceptionHandler.this.messageWriters;
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return JsonExceptionHandler.this.viewResolvers;
        }
    }
}

