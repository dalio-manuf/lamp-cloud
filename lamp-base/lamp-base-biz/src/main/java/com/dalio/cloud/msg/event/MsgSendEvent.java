package com.dalio.cloud.msg.event;

import org.springframework.context.ApplicationEvent;

/**
 * 登录事件
 *
 * @author admin
 * @date 2020年03月18日17:22:55
 */
public class MsgSendEvent extends ApplicationEvent {
    public MsgSendEvent(MsgEventVO msg) {
        super(msg);
    }
}
