package com.dalio.cloud.msg.controller;

import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.annotation.user.LoginUser;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperController;
import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.database.mybatis.conditions.query.QueryWrap;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.datascope.DataScopeHelper;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.enumeration.SourceType;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.dalio.cloud.msg.vo.query.ExtendMsgPageQuery;
import com.dalio.cloud.msg.vo.result.ExtendMsgResultVO;
import com.dalio.cloud.msg.vo.save.ExtendMsgSaveVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgPublishVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * 消息
 * </p>
 *
 * @author admin
 * @date 2022-07-10 11:41:17
 * @create [2022-07-10 11:41:17] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/extendMsg")
@Tag(name = "消息")
public class ExtendMsgController extends SuperController<ExtendMsgService, Long, ExtendMsg, ExtendMsgSaveVO,
        ExtendMsgUpdateVO, ExtendMsgPageQuery, ExtendMsgResultVO> {
    private final EchoService echoService;
    private final MsgBiz msgBiz;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Operation(summary = "根据模板发送消息", description = "根据模板发送消息")
    @PostMapping("/sendByTemplate")
    @WebLog("发送消息")
    public R<Boolean> sendByTemplate(@RequestBody @Validated(SuperEntity.Update.class) ExtendMsgSendVO data, @Parameter(hidden = true) @LoginUser(isEmployee = true) SysUser sysUser) {
        return R.success(msgBiz.sendByTemplate(data, sysUser));
    }

    @Operation(summary = "发布站内信", description = "发布站内信")
    @PostMapping("/publish")
    @WebLog("发布站内信")
    public R<Boolean> publish(@RequestBody @Validated(SuperEntity.Update.class) ExtendMsgPublishVO data, @Parameter(hidden = true) @LoginUser(isEmployee = true) SysUser sysUser) {
        return R.success(msgBiz.publish(data, sysUser));
    }

    @Override
    public QueryWrap<ExtendMsg> handlerWrapper(ExtendMsg model, PageParams<ExtendMsgPageQuery> params) {
        QueryWrap<ExtendMsg> queryWrap = super.handlerWrapper(model, params);
        queryWrap.lambda().eq(ExtendMsg::getChannel, SourceType.APP);
        DataScopeHelper.startDataScope("extend_msg");
        return queryWrap;
    }

    /**
     * 查询消息中心
     *
     * @param id 主键id
     * @return 查询结果
     */
    @Operation(summary = "查询消息中心", description = "查询消息中心")
    @GetMapping("/{id:[0-9]+}")
    @WebLog("查询消息中心")
    @Override
    public R<ExtendMsgResultVO> get(@PathVariable Long id) {
        return R.success(superService.getResultById(id));
    }


}


