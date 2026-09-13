package com.dalio.cloud.common.cache.common;


import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 参数 KEY
 * <p>
 * #c_application
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class TokenUserIdCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey builder(String uuid) {
        return new TokenUserIdCacheKeyBuilder().key(uuid);
    }

    public static CacheKey builder(String uuid, Long expire) {
        CacheKey cacheKey = builder(uuid);
        if (expire != null) {
            cacheKey.setExpire(Duration.ofSeconds(expire));
        }
        return cacheKey;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.TOKEN_USER_ID;
    }

    

    

    @Override
    public String getModular() {
        return CacheKeyModular.OAUTH;
    }

    @Override
    public String getField() {
        return null;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.string;
    }

    /**
     * 虽然这里配置了过期时间，但在实际使用时，业务代码从yml中读取了有效期，并覆盖了次参数，所以修改这里的有效期 不会生效！
     */
    @Override
    public Duration getExpire() {
        return Duration.ofHours(8L);
    }
}
