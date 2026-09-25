package com.xxl.job.executor.service.jobhandler;

import cn.hutool.core.convert.Convert;
import com.dalio.basic.jackson.JsonUtil;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基础业务 定时任务处理器
 *
 * @author dalio
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BaseJob {
    private final MsgBiz msgBiz;
    private final ExtendMsgService extendMsgService;

    @XxlJob("sendMsg")
    public void sendMsg() {
        String param = XxlJobHelper.getJobParam();
        ArgumentAssert.notEmpty(param, "参数不能为空");
        Map<?, ?> map = JsonUtil.parse(param, Map.class);
        if (map == null || map.isEmpty()) {
            XxlJobHelper.log("参数解析为空，跳过执行: {}", param);
            return;
        }
        Object msgIdObj = map.get("msgId");
        Long msgId = Convert.toLong(msgIdObj, null);
        if (msgId == null) {
            XxlJobHelper.log("msgId 解析为空或无效: {}", msgIdObj);
            XxlJobHelper.handleFail("msgId 参数无效: " + msgIdObj);
            return;
        }
        XxlJobHelper.log("开始执行消息发送任务, msgId={}", msgId);
        try {
            msgBiz.execSend(msgId);
            XxlJobHelper.log("消息发送任务执行成功, msgId={}", msgId);
        } catch (Exception e) {
            log.error("执行消息发送任务异常, msgId={}", msgId, e);
            XxlJobHelper.log("执行消息发送任务异常, msgId={}, error={}", msgId, e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    @XxlJob("publishMsg")
    public void publishMsg() {
        String param = XxlJobHelper.getJobParam();
        ArgumentAssert.notEmpty(param, "参数不能为空");
        Map<?, ?> map = JsonUtil.parse(param, Map.class);
        if (map == null || map.isEmpty()) {
            XxlJobHelper.log("参数解析为空，跳过执行: {}", param);
            return;
        }
        Object msgIdObj = map.get("msgId");
        Long msgId = Convert.toLong(msgIdObj, null);
        if (msgId == null) {
            XxlJobHelper.log("msgId 解析为空或无效: {}", msgIdObj);
            XxlJobHelper.handleFail("msgId 参数无效: " + msgIdObj);
            return;
        }
        XxlJobHelper.log("开始执行公告发布任务, msgId={}", msgId);
        try {
            extendMsgService.publishNotice(msgId);
            XxlJobHelper.log("公告发布任务执行成功, msgId={}", msgId);
        } catch (Exception e) {
            log.error("执行公告发布任务异常, msgId={}", msgId, e);
            XxlJobHelper.log("执行公告发布任务异常, msgId={}, error={}", msgId, e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
