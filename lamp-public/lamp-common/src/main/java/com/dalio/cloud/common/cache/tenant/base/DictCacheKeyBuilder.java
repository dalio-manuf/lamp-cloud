package com.dalio.cloud.common.cache.tenant.base;


import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.model.cache.CacheHashKey;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyModular;
import com.dalio.cloud.common.cache.CacheKeyTable;

import java.io.Serializable;

/**
 * 参数 KEY
 * <p>
 * key: lc:def_dict:tenant:id:string:{dict_key}
 * field1: {item_key1} --> item_name
 * field2: {item_key2} --> item_name
 *
 * <p>
 * #def_dict
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class DictCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey builder(Serializable dictKey) {
        return new DictCacheKeyBuilder().hashKey(dictKey);
    }

    public static CacheHashKey builder(String dictKey, String itemKey) {
        return new DictCacheKeyBuilder().hashFieldKey(itemKey, dictKey);
    }


    @Override
    public String getTable() {
        return CacheKeyTable.System.DICT;
    }

    @Override
    public String getModular() {
        return CacheKeyModular.SYSTEM;
    }

    @Override
    public String getField() {
        return SuperEntity.ID_FIELD;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.string;
    }
}
