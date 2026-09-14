package com.dalio.cloud.msg.ws;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.jackson.JsonUtil;
import com.dalio.basic.utils.BeanPlusUtil;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.msg.entity.ExtendNotice;
import com.dalio.cloud.msg.enumeration.NoticeRemindModeEnum;
import com.dalio.cloud.msg.service.ExtendNoticeService;
import com.dalio.cloud.msg.vo.MyMsgResult;
import com.dalio.cloud.msg.vo.result.ExtendNoticeResultVO;

import java.io.IOException;
import java.util.Map;

/**
 * @author admin
 * @date 2021/8/4 23:47
 */
@ServerEndpoint("/anno/myMsg/{principal}")
@Component
@Slf4j
public class MsgEndpoint {


    /**
     * 连接成功
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("principal") String principal, Session session) {
        log.info("连接成功");
        WebSocketObserver observer = new WebSocketObserver(session);
        // get subject
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(principal);
        // register observer into subject
        subject.addObserver(observer);
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(@PathParam("principal") String principal, Session session) {
        log.info("连接关闭");
        // get subject
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(principal);

        // get observer
        WebSocketObserver observer = new WebSocketObserver(session);
        // delete observer from subject
        subject.deleteObserver(observer);

        // close session and close Web Socket connection
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            log.error("关闭 WebSocket session 异常: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 接收客户端发送的消息， 并返回数据给客户端
     *
     * @param text
     */
    @OnMessage
    public String onMsg(@PathParam("principal") String principal, String text) {
        if (StrUtil.isEmpty(text) || "ping".equals(text)) {
            return StrPool.EMPTY;
        }
        log.info("employeeId={}, text={}", principal, text);
        ContextUtil.setEmployeeId(principal);
        try {
            PageParams<ExtendNotice> params = new PageParams<>(1, 10);
            ExtendNoticeService superService = SpringUtils.getBean(ExtendNoticeService.class);

            IPage<ExtendNotice> todoList = params.buildPage(ExtendNotice.class);
            IPage<ExtendNotice> noticeList = params.buildPage(ExtendNotice.class);
            IPage<ExtendNotice> earlyWarningList = params.buildPage(ExtendNotice.class);
            superService.page(todoList, Wraps.<ExtendNotice>lbQ()
                    .eq(ExtendNotice::getRemindMode, NoticeRemindModeEnum.TO_DO.getValue())
                    .eq(ExtendNotice::getIsRead, false).eq(ExtendNotice::getRecipientId, ContextUtil.getEmployeeId()));
            superService.page(noticeList, Wraps.<ExtendNotice>lbQ()
                    .eq(ExtendNotice::getRemindMode, NoticeRemindModeEnum.NOTICE.getValue())
                    .eq(ExtendNotice::getIsRead, false).eq(ExtendNotice::getRecipientId, ContextUtil.getEmployeeId()));
            superService.page(earlyWarningList, Wraps.<ExtendNotice>lbQ()
                    .eq(ExtendNotice::getRemindMode, NoticeRemindModeEnum.EARLY_WARNING.getValue())
                    .eq(ExtendNotice::getIsRead, false).eq(ExtendNotice::getRecipientId, ContextUtil.getEmployeeId()));

            MyMsgResult result = MyMsgResult.builder()
                    .todoList(BeanPlusUtil.toBeanPage(todoList, ExtendNoticeResultVO.class))
                    .noticeList(BeanPlusUtil.toBeanPage(noticeList, ExtendNoticeResultVO.class))
                    .earlyWarningList(BeanPlusUtil.toBeanPage(earlyWarningList, ExtendNoticeResultVO.class))
                    .build();

            Map<String, Object> map = MapUtil.newHashMap();
            map.put("type", "2");
            map.put("data", result);
            return JsonUtil.toJson(map);
        } finally {
            ContextUtil.remove();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接异常: sessionId={}", session != null ? session.getId() : null, error);
    }
}
