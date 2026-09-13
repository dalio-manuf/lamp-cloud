package com.dalio.cloud.common.cache.base.system;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 角色的资源 KEY
 * <p>
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class RoleResourceCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long applicationId, Long roleId) {
        return new RoleResourceCacheKeyBuilder().key(applicationId, roleId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Base.ROLE_RESOURCE;
    }



    @Override
    public String getModular() {
        return CacheKeyModular.BASE;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.number;
    }
}
