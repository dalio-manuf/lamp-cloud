package com.dalio.cloud.msg.ws;

import cn.hutool.core.util.StrUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author admin
 * @date 2021/8/4 23:47
 */
@ServerEndpoint("/anno/test")
@Component
@Slf4j
public class TestEndpoint {

    /**
     * 连接成功
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("连接成功");
        WebSocketObserver observer = new WebSocketObserver(session);
        // get subject
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(session.getId());
        // register observer into subject
        subject.addObserver(observer);
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        log.info("连接关闭");
        // get subject
        WebSocketSubject subject = WebSocketSubject.Holder.getSubject(session.getId());

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
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public String onMsg(String text) {
        if (StrUtil.isEmpty(text)) {
            return "";
        }
        return "server 收到消息：" + text;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 连接异常: sessionId={}", session != null ? session.getId() : null, error);
    }
}
