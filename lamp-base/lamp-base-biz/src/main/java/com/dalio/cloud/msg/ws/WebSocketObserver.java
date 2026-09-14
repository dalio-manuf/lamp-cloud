package com.dalio.cloud.msg.ws;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

/**
 * Web Socket 观察者.</br>
 *
 * @author admin
 * @date 2021/8/5 14:59
 */
@Slf4j
public class WebSocketObserver implements Observer {

    /**
     * Web Socket session
     */
    private final Session session;

    public WebSocketObserver(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void update(Observable o, Object arg) {
        String message = (String) arg;
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.warn("WebSocket 发送消息失败, sessionId={}", session != null ? session.getId() : null, e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((session == null) ? 0 : session.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        WebSocketObserver other = (WebSocketObserver) obj;
        String thisId = session != null ? session.getId() : null;
        String otherId = other.session != null ? other.session.getId() : null;
        return Objects.equals(thisId, otherId);
    }

}
