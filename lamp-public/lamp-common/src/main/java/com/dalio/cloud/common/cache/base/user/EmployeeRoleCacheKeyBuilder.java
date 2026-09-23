package com.dalio.cloud.common.cache.base.user;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 用户角色
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class EmployeeRoleCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey build(Long employeeId) {
        return new EmployeeRoleCacheKeyBuilder().key(employeeId);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Base.EMPLOYEE_ROLE;
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
    public ValueType getValueType() {
        return ValueType.number;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofHours(24);
    }
}
