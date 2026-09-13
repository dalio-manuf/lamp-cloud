package com.dalio.cloud.system.vo.result.system;

import cn.dev33.satoken.session.SaTerminalInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 在线用户 token
 * @author admin
 * @since 2024/8/1 15:40
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OnlineTokenResultVO extends SaTerminalInfo {

    /** 创建时间 */
    private LocalDateTime sessionTime;
    private String sessionStr;
    /**  失效时间 */
    private LocalDateTime expireTime;
    private String expireStr;
}
