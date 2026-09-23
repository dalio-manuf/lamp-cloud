package com.dalio.cloud.common.cache.base.common;


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
 * key: dict:{dict_key}
 * field1: {item_key1} --> item_name
 * field2: {item_key2} --> item_name
 *
 * <p>
 * #c_dictionary_item
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class BaseDictCacheKeyBuilder implements CacheKeyBuilder {
    public static CacheKey builder(Serializable dictKey) {
        return new BaseDictCacheKeyBuilder().hashKey(dictKey);
    }

    public static CacheHashKey builder(String dictKey, String field) {
        return new BaseDictCacheKeyBuilder().hashFieldKey(field, dictKey);
    }


    @Override
    public String getTable() {
        return CacheKeyTable.Base.BASE_DICT;
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
        return ValueType.string;
    }
}
