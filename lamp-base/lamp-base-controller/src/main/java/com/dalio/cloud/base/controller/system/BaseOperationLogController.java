package com.dalio.cloud.base.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.base.entity.system.BaseOperationLog;
import com.dalio.cloud.base.service.system.BaseOperationLogService;
import com.dalio.cloud.base.vo.query.system.BaseOperationLogPageQuery;
import com.dalio.cloud.base.vo.result.system.BaseOperationLogResultVO;
import com.dalio.cloud.base.vo.save.system.BaseOperationLogSaveVO;
import com.dalio.cloud.base.vo.update.system.BaseOperationLogUpdateVO;

import java.time.LocalDateTime;


/**
 * <p>
 * 前端控制器
 * 操作日志
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/baseOperationLog")
@Tag(name = "操作日志")
public class BaseOperationLogController extends SuperController<BaseOperationLogService, Long, BaseOperationLog, BaseOperationLogSaveVO, BaseOperationLogUpdateVO, BaseOperationLogPageQuery, BaseOperationLogResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public R<BaseOperationLogResultVO> getDetail(@RequestParam("id") Long id) {
        return R.success(superService.getDetail(id));
    }

    @Operation(summary = "清空日志", description = "清空日志")
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
