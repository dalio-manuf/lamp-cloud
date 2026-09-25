package com.dalio.cloud.system.controller.anyone;

import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.common.constant.AppendixType;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.service.application.DefApplicationService;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author admin
 * @date 2021/10/26 21:40
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/anyone")
@Tag(name = "需要登录但无需验证uri权限的接口")
public class TenantAnyoneController {

    private final DefApplicationService defApplicationService;
    private final EchoService echoService;
    private final AppendixService appendixService;


    @Operation(summary = "设置我的默认应用")
    @PostMapping("/updateDefApp")
    @WebLog(value = "设置我的默认应用")
    public R<Boolean> updateDefApp(@RequestParam Long applicationId) {
        return R.success(defApplicationService.updateDefApp(applicationId, ContextUtil.getUserId()));
    }

    @Operation(summary = "查询我的默认应用")
    @GetMapping("/getDefApp")
    @WebLog(value = "查询我的默认应用")
    public R<DefApplication> getDefApp() {
        return R.success(defApplicationService.getDefApp(ContextUtil.getUserId()));
    }

    @Operation(summary = "查询我的应用")
    @GetMapping("/findMyApplication")
    @WebLog(value = "查询我的应用")
    public R<List<DefApplicationResultVO>> findMyApplication(@RequestParam(required = false) String name) {
        List<DefApplicationResultVO> list = defApplicationService.findMyApplication(name);
        echoService.action(list);
        appendixService.echoAppendix(list, AppendixType.System.DEF__APPLICATION__LOGO);
        return R.success(list);
    }

    @Operation(summary = "查询推荐应用")
    @GetMapping("/findRecommendApplication")
    @WebLog(value = "查询推荐应用")
    public R<List<DefApplicationResultVO>> findRecommendApplication(@RequestParam(required = false) String name) {
        List<DefApplicationResultVO> list = defApplicationService.findRecommendApplication(name);
        echoService.action(list);
        appendixService.echoAppendix(list, AppendixType.System.DEF__APPLICATION__LOGO);
        return R.success(list);
    }

}
