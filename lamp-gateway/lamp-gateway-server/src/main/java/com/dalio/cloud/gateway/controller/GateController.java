package com.dalio.cloud.gateway.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import com.dalio.basic.base.R;
import com.dalio.cloud.model.vo.result.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网关常用基础服务控制器
 *
 * @author dalio
 * @since 2019-06-21 18:22
 */
@RestController
@Tag(name = "网关常用接口")
@RequiredArgsConstructor
public class GateController {

    private final DiscoveryClient discoveryClient;
    private final GatewayProperties gatewayProperties;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${spring.application.name:}")
    private String application;

    /**
     * Swagger UI 文档跳转（兼容原 doc.html 访问）
     */
    @Operation(summary = "Swagger UI 文档跳转")
    @GetMapping({"/gate/swagger-ui.html", "/gate/doc.html"})
    public Rendering doc() {
        String uri = String.format("%s/swagger-ui.html", StrUtil.nullToEmpty(contextPath));
        return Rendering.redirectTo(uri).build();
    }

    @Operation(summary = "查询在线服务的前缀")
    @GetMapping("/gateway/findOnlineServicePrefix")
    public R<Map<String, String>> findOnlineServicePrefix() {
        List<String> services = discoveryClient.getServices();

        Map<String, String> map = MapUtil.newHashMap();
        services.forEach(service ->
                gatewayProperties.getRoutes().forEach(route -> {
                    if (route.getUri() != null && StrUtil.equalsIgnoreCase(service, route.getUri().getHost())) {
                        if (CollUtil.isEmpty(route.getPredicates())) {
                            return;
                        }
                        PredicateDefinition predicateDefinition = route.getPredicates().get(0);
                        predicateDefinition.getArgs().forEach((k, v) ->
                                map.put(service, StrUtil.subBetween(v, "/", "/**"))
                        );
                    }
                })
        );
        map.put(application, "gateway");
        return R.success(map);
    }

    @Operation(summary = "查询在线服务")
    @GetMapping("/gateway/findOnlineService")
    public R<List<Option>> findOnlineService() {
        List<String> services = discoveryClient.getServices();

        List<Option> list = new ArrayList<>();
        services.forEach(service ->
                gatewayProperties.getRoutes().forEach(route -> {
                    if (route.getUri() != null && StrUtil.equalsIgnoreCase(service, route.getUri().getHost())) {
                        if (CollUtil.isEmpty(route.getPredicates())) {
                            return;
                        }
                        PredicateDefinition predicateDefinition = route.getPredicates().get(0);
                        predicateDefinition.getArgs().forEach((k, v) ->
                                list.add(Option.builder()
                                        .value(StrUtil.subBetween(v, "/", "/**"))
                                        .remark(service)
                                        .label(service)
                                        .build())
                        );
                    }
                })
        );
        return R.success(list);
    }
}

