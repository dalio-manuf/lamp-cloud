package com.dalio.cloud.msg.controller;

import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.msg.entity.ExtendInterfaceLogging;
import com.dalio.cloud.msg.service.ExtendInterfaceLoggingService;
import com.dalio.cloud.msg.vo.query.ExtendInterfaceLoggingPageQuery;
import com.dalio.cloud.msg.vo.result.ExtendInterfaceLoggingResultVO;
import com.dalio.cloud.msg.vo.save.ExtendInterfaceLoggingSaveVO;
import com.dalio.cloud.msg.vo.update.ExtendInterfaceLoggingUpdateVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 接口执行日志记录
 * </p>
 *
 * @author admin
 * @date 2022-07-09 23:58:59
 * @create [2022-07-09 23:58:59] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/extendInterfaceLogging")
@Tag(name = "接口执行日志记录")
public class ExtendInterfaceLoggingController extends SuperController<ExtendInterfaceLoggingService, Long, ExtendInterfaceLogging, ExtendInterfaceLoggingSaveVO,
        ExtendInterfaceLoggingUpdateVO, ExtendInterfaceLoggingPageQuery, ExtendInterfaceLoggingResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

}


