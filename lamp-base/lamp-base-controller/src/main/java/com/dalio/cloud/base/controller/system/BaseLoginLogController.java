package com.dalio.cloud.base.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.service.system.DefLoginLogService;
import com.dalio.cloud.system.vo.query.system.DefLoginLogPageQuery;
import com.dalio.cloud.system.vo.result.system.DefLoginLogResultVO;
import com.dalio.cloud.system.vo.save.system.DefLoginLogSaveVO;
import com.dalio.cloud.system.vo.update.system.DefLoginLogUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


/**
 * <p>
 * 前端控制器
 * 登录日志
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/baseLoginLog")
@Tag(name = "登录日志")
public class BaseLoginLogController extends SuperController<DefLoginLogService, Long, DefLoginLog, DefLoginLogSaveVO,
        DefLoginLogUpdateVO, DefLoginLogPageQuery, DefLoginLogResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Operation(summary = "任何人分页查询登录日志")
    @PostMapping(value = "/anyone/page")
    @WebLog(value = "'任何人分页查询登录日志:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<IPage<DefLoginLogResultVO>> anyOnePage(@RequestBody PageParams<DefLoginLogPageQuery> params) {
        return super.page(params);
    }


    @Operation(summary = "清空日志")
    @DeleteMapping("clear")
    @WebLog("清空日志")
    public R<Boolean> clear(@RequestParam(required = false, defaultValue = "1") Integer type) {
        int t = type != null ? type : 1;
        LocalDateTime clearBeforeTime = switch (t) {
            case 1 -> LocalDateTime.now().minusMonths(1);
            case 2 -> LocalDateTime.now().minusMonths(3);
            case 3 -> LocalDateTime.now().minusMonths(6);
            case 4 -> LocalDateTime.now().minusMonths(12);
            default -> null;
        };
        Integer clearBeforeNum = switch (t) {
            case 5 -> 1000;
            case 6 -> 10000;
            case 7 -> 30000;
            case 8 -> 100000;
            default -> null;
        };

        return success(superService.clearLog(clearBeforeTime, clearBeforeNum));
    }
}
