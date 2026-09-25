package com.dalio.cloud.system.controller.tenant;

import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.entity.tenant.DefDatasourceConfig;
import com.dalio.cloud.system.service.tenant.DefDatasourceConfigService;
import com.dalio.cloud.system.vo.query.tenant.DefDatasourceConfigPageQuery;
import com.dalio.cloud.system.vo.result.tenant.DefDatasourceConfigResultVO;
import com.dalio.cloud.system.vo.save.tenant.DefDatasourceConfigSaveVO;
import com.dalio.cloud.system.vo.update.tenant.DefDatasourceConfigUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 前端控制器
 * 数据源
 * </p>
 *
 * @author admin
 * @date 2021-09-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defDatasourceConfig")
@Tag(name = "数据源")
public class DefDatasourceConfigController extends SuperController<DefDatasourceConfigService, Long, DefDatasourceConfig, DefDatasourceConfigSaveVO, DefDatasourceConfigUpdateVO, DefDatasourceConfigPageQuery, DefDatasourceConfigResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Operation(summary = "测试数据库链接")
    @PostMapping("/testConnect")
    public R<Boolean> testConnect(@RequestParam Long id) {
        return R.success(superService.testConnection(id));
    }
}
