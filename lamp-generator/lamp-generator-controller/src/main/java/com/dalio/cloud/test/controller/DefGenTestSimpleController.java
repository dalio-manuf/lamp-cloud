package com.dalio.cloud.test.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.test.entity.DefGenTestSimple;
import com.dalio.cloud.test.service.DefGenTestSimpleService;
import com.dalio.cloud.test.vo.query.DefGenTestSimplePageQuery;
import com.dalio.cloud.test.vo.result.DefGenTestSimpleResultVO;
import com.dalio.cloud.test.vo.save.DefGenTestSimpleSaveVO;
import com.dalio.cloud.test.vo.update.DefGenTestSimpleUpdateVO;

/**
 * <p>
 * 前端控制器
 * 测试单表
 * </p>
 *
 * @author admin
 * @date 2022-04-15 15:36:45
 * @create [2022-04-15 15:36:45] [admin] [代码生成器生成]
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defGenTestSimple")
@Tag(name = "测试单表")
public class DefGenTestSimpleController extends SuperController<DefGenTestSimpleService, Long, DefGenTestSimple, DefGenTestSimpleSaveVO,
        DefGenTestSimpleUpdateVO, DefGenTestSimplePageQuery, DefGenTestSimpleResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

}


