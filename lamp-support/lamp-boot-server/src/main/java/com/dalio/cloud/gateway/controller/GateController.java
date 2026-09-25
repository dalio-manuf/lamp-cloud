package com.dalio.cloud.gateway.controller;

import com.dalio.basic.base.R;
import com.dalio.cloud.model.vo.result.Option;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 常用网关/服务列表 Controller（单体版）
 *
 * @author dalio
 * @since 2019-06-21 18:22
 */
@RestController
@Tag(name = "网关常用接口")
public class GateController {

    @Value("${spring.application.name:}")
    private String application;

    @Operation(summary = "查询在线服务的前缀")
    @GetMapping("/gateway/findOnlineServicePrefix")
    public R<Map<String, String>> findOnlineServicePrefix() {
        return R.success(Map.of(application, "base"));
    }

    @Operation(summary = "查询在线服务")
    @GetMapping("/gateway/findOnlineService")
    public R<List<Option>> findOnlineService() {
        List<Option> list = List.of(
                Option.builder().value("base").remark(application).label("lamp-base").build(),
                Option.builder().value("system").remark(application).label("lamp-system").build(),
                Option.builder().value("oauth").remark(application).label("lamp-oauth").build(),
                Option.builder().value("generator").remark(application).label("lamp-generator").build()
        );
        return R.success(list);
    }
}
