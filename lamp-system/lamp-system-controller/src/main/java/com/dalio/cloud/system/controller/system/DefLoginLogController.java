package com.dalio.cloud.system.controller.system;

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
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.service.system.DefLoginLogService;
import com.dalio.cloud.system.vo.query.system.DefLoginLogPageQuery;
import com.dalio.cloud.system.vo.result.system.DefLoginLogResultVO;
import com.dalio.cloud.system.vo.save.system.DefLoginLogSaveVO;
import com.dalio.cloud.system.vo.update.system.DefLoginLogUpdateVO;

import java.time.LocalDateTime;


/**
 * <p>
 * 前端控制器
 * 登录日志
 * </p>
 *
 * @author admin
 * @date 2021-11-12
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defLoginLog")
@Tag(name = "登录日志")
public class DefLoginLogController extends SuperController<DefLoginLogService, Long, DefLoginLog, DefLoginLogSaveVO,
        DefLoginLogUpdateVO, DefLoginLogPageQuery, DefLoginLogResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Operation(summary = "清空日志")
    @DeleteMapping("clear")
    @WebLog("清空日志")
    public R<Boolean> clear(@RequestParam(required = false, defaultValue = "1") Integer type) {
        LocalDateTime clearBeforeTime = null;
        Integer clearBeforeNum = null;
        int clearType = type != null ? type : 1;
        switch (clearType) {
            case 1:
                clearBeforeTime = LocalDateTime.now().minusMonths(1);
                break;
            case 2:
                clearBeforeTime = LocalDateTime.now().minusMonths(3);
                break;
            case 3:
                clearBeforeTime = LocalDateTime.now().minusMonths(6);
                break;
            case 4:
                clearBeforeTime = LocalDateTime.now().minusMonths(12);
                break;
            case 5:
                clearBeforeNum = 1000;
                break;
            case 6:
                clearBeforeNum = 10000;
                break;
            case 7:
                clearBeforeNum = 30000;
                break;
            case 8:
                clearBeforeNum = 100000;
                break;
            default:
                return R.fail("非法的日志清理类型参数");
        }
        return success(superService.clearLog(clearBeforeTime, clearBeforeNum));
    }
}
