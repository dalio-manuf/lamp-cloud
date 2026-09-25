package com.dalio.cloud.system.manager.system.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.dalio.basic.base.manager.impl.SuperCacheManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.basic.utils.CollHelper;
import com.dalio.cloud.common.cache.tenant.base.DictParameterKeyBuilder;
import com.dalio.cloud.system.entity.system.DefParameter;
import com.dalio.cloud.system.manager.system.DefParameterManager;
import com.dalio.cloud.system.mapper.system.DefParameterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * 通用业务实现类
 * 参数配置
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 * @create [2021-10-13] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefParameterManagerImpl extends SuperCacheManagerImpl<DefParameterMapper, DefParameter> implements DefParameterManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new DictParameterKeyBuilder();
    }

    @Override

    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return CollHelper.uniqueIndex(find(ids), DefParameter::getId, DefParameter::getName);
    }

    public List<DefParameter> find(Set<Serializable> ids) {
        // 强转， 防止数据库隐式转换，  若你的id 是string类型，请勿强转
        return super.listByIds(ids.stream().filter(Objects::nonNull).map(Convert::toLong).toList());
    }

    @Override

    public Map<String, String> findParamMapByKey(List<String> paramsKeys) {
        if (CollUtil.isEmpty(paramsKeys)) {
            return Collections.emptyMap();
        }
        LbQueryWrap<DefParameter> query = Wraps.<DefParameter>lbQ().in(DefParameter::getKey, paramsKeys).eq(DefParameter::getState, true);
        List<DefParameter> list = super.list(query);

        //key 是类型
        return CollHelper.uniqueIndex(list, DefParameter::getKey, DefParameter::getValue);
    }
}
