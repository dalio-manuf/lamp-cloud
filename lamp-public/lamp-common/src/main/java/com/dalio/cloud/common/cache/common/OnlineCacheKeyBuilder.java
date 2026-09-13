package com.dalio.cloud.common.cache.common;


import cn.hutool.core.util.StrUtil;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyTable;

/**
 * online:{userid} -> token (String)
 * <p>
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class OnlineCacheKeyBuilder implements CacheKeyBuilder {
    @Override
    public String getTable() {
        return CacheKeyTable.ONLINE;
    }

    /**
     * 获取通配符
     *
     * @return key 前缀
     */
    @Override
    public String getPattern() {
        return StrUtil.format("{}:*", getTable());
    }
}
