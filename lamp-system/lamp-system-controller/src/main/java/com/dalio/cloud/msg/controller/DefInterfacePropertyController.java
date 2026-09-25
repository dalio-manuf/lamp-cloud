package com.dalio.cloud.msg.controller;

import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import com.dalio.cloud.msg.service.DefInterfacePropertyService;
import com.dalio.cloud.msg.vo.query.DefInterfacePropertyPageQuery;
import com.dalio.cloud.msg.vo.result.DefInterfacePropertyResultVO;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertyBatchSaveVO;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertySaveVO;
import com.dalio.cloud.msg.vo.update.DefInterfacePropertyUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 接口属性
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/defInterfaceProperty")
@Tag(name = "接口属性")
public class DefInterfacePropertyController extends SuperController<DefInterfacePropertyService, Long, DefInterfaceProperty, DefInterfacePropertySaveVO,
        DefInterfacePropertyUpdateVO, DefInterfacePropertyPageQuery, DefInterfacePropertyResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    /**
     * 新增
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存")
    @PostMapping("/batchSave")
    @WebLog(value = "保存", request = false)
    public R<Boolean> batchSave(@RequestBody @Validated DefInterfacePropertyBatchSaveVO saveVO) {
        return R.success(superService.batchSave(saveVO));

    }

}


