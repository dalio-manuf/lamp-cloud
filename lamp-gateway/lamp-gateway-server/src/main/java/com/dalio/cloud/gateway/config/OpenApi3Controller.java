package com.dalio.cloud.gateway.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import com.dalio.basic.utils.StrPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * OpenAPI 3 / Swagger 文档聚合控制器
 *
 * @author dalio
 * @date 2019/07/31
 */
@RestController
@Slf4j
public class OpenApi3Controller {
    public static final String SWAGGER_RESOURCES_URI = "/v3/api-docs/swagger-config";

    private final GatewayProperties gatewayProperties;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final ExecutorService executorService = new ThreadPoolExecutor(
            2, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNamePrefix("openapi-aggregator-").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    public OpenApi3Controller(GatewayProperties gatewayProperties,
                              @Qualifier("lbRestTemplate") RestTemplate restTemplate,
                              DiscoveryClient discoveryClient) {
        this.gatewayProperties = gatewayProperties;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }

    @GetMapping(SWAGGER_RESOURCES_URI)
    public Mono<ResponseEntity<Map<String, Object>>> swaggerResources() {
        return Mono.just(new ResponseEntity<>(get(), HttpStatus.OK));
    }

    private Map<String, Object> get() {
        List<Map<String, Object>> list = gatewayProperties.getRoutes().stream()
                .map(this::swaggerConfig)
                .filter(Objects::nonNull)
                .toList();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        map.putAll(list.get(0));

        for (int i = 1; i < list.size(); i++) {
            Map<String, Object> item = list.get(i);
            if (map.containsKey("urls")) {
                @SuppressWarnings("unchecked")
                List<Object> urls = (List<Object>) item.get("urls");
                @SuppressWarnings("unchecked")
                List<Object> targetUrls = (List<Object>) map.get("urls");
                if (urls != null && targetUrls != null) {
                    targetUrls.addAll(urls);
                }
            } else {
                @SuppressWarnings("unchecked")
                List<Object> urls = (List<Object>) item.get("urls");
                map.put("urls", urls != null ? urls : Collections.emptyList());
            }
        }
        return map;
    }

    private Map<String, Object> swaggerConfig(RouteDefinition route) {
        try {
            if (route.getUri() == null || route.getUri().getHost() == null) {
                return null;
            }
            String host = route.getUri().getHost();
            // 聚合各个微服务的所有 group 分组文档
            List<ServiceInstance> instances = discoveryClient.getInstances(host);
            if (CollUtil.isEmpty(instances)) {
                return null;
            }
            // 异步调用避免阻塞响应式主线程，设置 3 秒超时防卡死
            Future<Map<String, Object>> future = executorService.submit(() -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject("http://" + host + SWAGGER_RESOURCES_URI, Map.class);
                return response;
            });
            Map<String, Object> map = future.get(3, TimeUnit.SECONDS);
            if (map == null || map.isEmpty()) {
                return null;
            }
            map.forEach((k, v) -> {
                if ("urls".equals(k) && v instanceof List<?> list) {
                    for (Object obj : list) {
                        if (obj instanceof Map<?, ?> rawMap) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> item = (Map<String, Object>) rawMap;
                            String url = (String) item.get("url");
                            item.put("url", contextPath + StrPool.SLASH + route.getId() + url);
                            item.put("contextPath", contextPath + StrPool.SLASH + route.getId());
                        }
                    }
                }
            });

            return map;
        } catch (Exception e) {
            log.debug("加载 {} 的 Swagger 文档信息失败: {}", route.getUri() != null ? route.getUri().getHost() : "", e.getMessage());
        }
        return null;
    }
}


