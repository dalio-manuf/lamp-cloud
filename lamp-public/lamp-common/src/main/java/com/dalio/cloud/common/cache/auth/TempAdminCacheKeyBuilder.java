package com.dalio.cloud.common.cache.auth;

import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;

import java.time.Duration;

/**
 * 临时管理员账号
 * @author admin
 * @since 2025/11/19 11:08
 */
public class TempAdminCacheKeyBuilder implements CacheKeyBuilder {

    public static CacheKey builder(String username) {
        return new TempAdminCacheKeyBuilder().key(username);
    }

    public static CacheKey builder(String username, String type) {
        return new TempAdminCacheKeyBuilder().key(username, type);
    }

    @Override
    public String getTable() {
        return "admin_user";
    }


    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
