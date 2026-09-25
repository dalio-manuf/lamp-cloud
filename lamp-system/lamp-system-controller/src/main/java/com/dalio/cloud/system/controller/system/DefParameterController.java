package com.dalio.cloud.system.controller.system;

import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperCacheController;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.database.mybatis.conditions.query.QueryWrap;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.entity.system.DefParameter;
import com.dalio.cloud.system.service.system.DefParameterService;
import com.dalio.cloud.system.vo.query.system.DefParameterPageQuery;
import com.dalio.cloud.system.vo.result.system.DefParameterResultVO;
import com.dalio.cloud.system.vo.save.system.DefParameterSaveVO;
import com.dalio.cloud.system.vo.update.system.DefParameterUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.dalio.cloud.common.constant.SwaggerConstants.DATA_TYPE_LONG;
import static com.dalio.cloud.common.constant.SwaggerConstants.DATA_TYPE_STRING;


/**
 * <p>
 * 前端控制器
 * 参数配置
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defParameter")
@Tag(name = "参数配置")
public class DefParameterController extends SuperCacheController<DefParameterService, Long, DefParameter, DefParameterSaveVO, DefParameterUpdateVO, DefParameterPageQuery, DefParameterResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<DefParameter> handlerWrapper(DefParameter model, PageParams<DefParameterPageQuery> params) {
        QueryWrap<DefParameter> wrap = super.handlerWrapper(null, params);
        LbQueryWrap<DefParameter> wrapper = wrap.lambda();
        wrapper.like(DefParameter::getKey, model.getKey())
                .like(DefParameter::getName, model.getName())
                .like(DefParameter::getValue, model.getValue())
                .like(DefParameter::getRemarks, model.getRemarks())
                .in(DefParameter::getState, params.getModel().getState());
        return wrap;
    }


    @Parameters({
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "key", description = "参数键", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测参数键是否可用")
    @GetMapping("/check")
    public R<Boolean> check(@RequestParam String key, @RequestParam(required = false) Long id) {
        return success(superService.checkKey(key, id));
    }
}
