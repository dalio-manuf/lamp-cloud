package com.dalio.cloud.system.manager.system;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.system.entity.system.DefLoginLog;

import java.time.LocalDateTime;

/**
 * <p>
 * 通用业务接口
 * 登录日志
 * </p>
 *
 * @author admin
 * @date 2021-11-12
 */
public interface DefLoginLogManager extends SuperManager<DefLoginLog> {
    /**
     * 清理日志
     *
     * @param clearBeforeTime 多久之前的
     * @param clearBeforeNum  多少条
     * @return 是否成功
     */
    Long clearLog(LocalDateTime clearBeforeTime, Integer clearBeforeNum);
}
