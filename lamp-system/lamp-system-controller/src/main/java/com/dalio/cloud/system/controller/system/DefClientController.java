package com.dalio.cloud.system.controller.system;

import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.entity.system.DefClient;
import com.dalio.cloud.system.service.system.DefClientService;
import com.dalio.cloud.system.vo.query.system.DefClientPageQuery;
import com.dalio.cloud.system.vo.result.system.DefClientResultVO;
import com.dalio.cloud.system.vo.save.system.DefClientSaveVO;
import com.dalio.cloud.system.vo.update.system.DefClientUpdateVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 前端控制器
 * 客户端
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defClient")
@Tag(name = "客户端")
public class DefClientController extends SuperController<DefClientService, Long, DefClient, DefClientSaveVO, DefClientUpdateVO, DefClientPageQuery, DefClientResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

}
