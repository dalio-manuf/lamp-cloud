package com.dalio.cloud.system.manager.system;

import com.dalio.basic.base.manager.SuperCacheManager;
import com.dalio.cloud.system.entity.system.DefClient;

/**
 * <p>
 * 通用业务接口
 * 客户端
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
public interface DefClientManager extends SuperCacheManager<DefClient> {
    /**
     * 根据 客户端id 和 客户端秘钥查询应用
     *
     * @param clientId
     * @param clientSecret
     * @return
     */
    DefClient getClient(String clientId, String clientSecret);
}
