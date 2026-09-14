package com.dalio.cloud.gateway.config;

import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 跨域配置与解码器注册
 *
 * @author dalio
 * @date 2017-12-29 14:16
 */
@Configuration
public class CorsConfiguration {
    private static final String ALL = "*";
    private static final String MAX_AGE = "18000";

    @Bean
    public RouteDefinitionLocator discoveryClientRouteDefinitionLocator(ReactiveDiscoveryClient discoveryClient,
                                                                        DiscoveryLocatorProperties properties) {
        return new DiscoveryClientRouteDefinitionLocator(discoveryClient, properties);
    }

    /**
     * 网关全局跨域过滤器
     * 处理浏览器预检请求（OPTIONS）及跨域响应头设置
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public WebFilter corsFilter() {
        return (ctx, chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(ctx);
            }
            HttpHeaders requestHeaders = request.getHeaders();
            ServerHttpResponse response = ctx.getResponse();
            HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
            HttpHeaders headers = response.getHeaders();

            String origin = requestHeaders.getOrigin();
            if (origin != null) {
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            }
            List<String> allowHeaders = requestHeaders.getAccessControlRequestHeaders();
            if (allowHeaders != null && !allowHeaders.isEmpty()) {
                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
            }
            if (requestMethod != null) {
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
            }
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, ALL);
            headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            return chain.filter(ctx);
        };
    }

    @Bean
    public ServerCodecConfigurer serverCodecConfigurer() {
        return new DefaultServerCodecConfigurer();
    }

    /**
     * 解决网关整合 Feign 时的表单与响应解码器配置
     *
     * @return Feign 解码器
     */
    @Bean
    public Decoder feignFormDecoder() {
        List<HttpMessageConverter<?>> converters = new RestTemplate().getMessageConverters();
        ObjectFactory<HttpMessageConverters> factory = () -> new HttpMessageConverters(converters);
        return new ResponseEntityDecoder(new SpringDecoder(factory));
    }
}

