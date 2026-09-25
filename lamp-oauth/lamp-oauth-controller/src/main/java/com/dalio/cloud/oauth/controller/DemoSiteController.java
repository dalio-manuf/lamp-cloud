package com.dalio.cloud.oauth.controller;

import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 演示站点专用接口
 *
 * @author admin
 * @since 2025/11/19 11:03
 */
@Slf4j
@RestController
@Tag(name = "演示站点专用接口")
@RequiredArgsConstructor
public class DemoSiteController {
    private final UserInfoService userInfoService;

    @Operation(summary = "注册临时管理员账号密码", description = "注册临时管理员账号密码")
    @PostMapping(value = "/anyTenant/registerTempAdmin")
    public R<Map<String, Object>> registerTempAdmin(@RequestParam String type) {
        return R.success(userInfoService.registerTempAdmin(type));
    }
}
