package com.dalio.cloud.system.service.system;

import com.dalio.basic.base.service.SuperCacheService;
import com.dalio.cloud.system.entity.system.DefParameter;

/**
 * <p>
 * 业务接口
 * 参数配置
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
public interface DefParameterService extends SuperCacheService<Long, DefParameter> {
    /**
     * ¬
     * 检测参数键是否可用
     *
     * @param key 健
     * @param id  参数ID
     * @return 是否存在
     */
    Boolean checkKey(String key, Long id);
}
