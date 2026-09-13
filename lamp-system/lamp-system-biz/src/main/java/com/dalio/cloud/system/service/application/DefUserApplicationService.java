package com.dalio.cloud.system.service.application;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.system.entity.application.DefUserApplication;

/**
 * <p>
 * 业务接口
 * 用户的默认应用
 * </p>
 *
 * @author admin
 * @date 2022-03-06
 */
public interface DefUserApplicationService extends SuperService<Long, DefUserApplication> {

    /**
     * 查询用户设置的默认应用
     *
     * @param userId 用户id
     * @return
     */
    Long getMyDefAppByUserId(Long userId);
}
