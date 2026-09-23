package com.dalio.cloud.common.cache.tenant.system;

import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 客户端
 * <p>
 * #def_client
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class DefClientSecretCacheKeyBuilder implements CacheKeyBuilder {

    public static CacheKey builder(String clientId, String clientSecret) {
        return new DefClientSecretCacheKeyBuilder().key(clientId, clientSecret);
    }


    @Override
    public String getTable() {
        return CacheKeyTable.System.DEF_CLIENT;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.SYSTEM;
    }

    @Override
    public String getField() {
        return "cid.secret";
    }

    @Override
    public ValueType getValueType() {
        return ValueType.string;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

}
