package com.dalio.cloud.base.manager.user;

import com.dalio.basic.base.manager.SuperCacheManager;
import com.dalio.basic.interfaces.echo.LoadService;
import com.dalio.cloud.base.entity.user.BaseOrg;

/**
 * <p>
 * 通用业务接口
 * 组织
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
public interface BaseOrgManager extends SuperCacheManager<BaseOrg>, LoadService {
}
