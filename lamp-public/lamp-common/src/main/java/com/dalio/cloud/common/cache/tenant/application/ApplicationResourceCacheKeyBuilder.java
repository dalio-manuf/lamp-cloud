package com.dalio.cloud.common.cache.tenant.application;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 应用 KEY
 * [服务模块名:]业务类型[:业务字段][:value类型][:应用id] -> obj
 * app_res:id:obj:1 -> {}
 * <p>
 * #def_resource
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class ApplicationResourceCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long applicationId) {
        return new ApplicationResourceCacheKeyBuilder().key(applicationId);
    }

    

    

    @Override
    public String getModular() {
        return CacheKeyModular.SYSTEM;
    }

    @Override
    public String getTable() {
        return CacheKeyTable.System.APPLICATION_RESOURCE;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.number;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
