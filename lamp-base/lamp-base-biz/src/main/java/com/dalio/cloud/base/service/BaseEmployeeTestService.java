package com.dalio.cloud.base.service;

import com.dalio.basic.base.manager.SuperCacheManager;
import com.dalio.cloud.base.entity.user.BaseEmployee;

/**
 * @author admin
 * @version v1.0
 * @date 2022/9/20 11:31 AM
 * @create [2022/9/20 11:31 AM ] [admin] [初始创建]
 */
public interface BaseEmployeeTestService extends SuperCacheManager<BaseEmployee> {
    /**
     * 单体查询
     *
     * @param id id
     * @return com.dalio.cloud.base.entity.user.BaseEmployee
     * @author admin
     * @date 2022/10/28 9:20 AM
     * @create [2022/10/28 9:20 AM ] [admin] [初始创建]
     */
    BaseEmployee get(Long id);
}
