package com.dalio.cloud.common.cache.common;


import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;
import java.time.LocalDate;

/**
 * 参数 KEY
 * {tenant}:TODAY_LOGIN_IV:{now} -> long
 * <p>
 * #c_login_log
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class TodayLoginIvCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(LocalDate now) {
        return new TodayLoginIvCacheKeyBuilder().key(now.toString());
    }

    @Override
    public String getTable() {
        return CacheKeyTable.TODAY_LOGIN_IV;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(2L);
    }
}
