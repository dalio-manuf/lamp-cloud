package com.dalio.cloud.base.manager.user.impl;

import cn.hutool.core.collection.CollUtil;
import com.dalio.basic.base.manager.impl.SuperCacheManagerImpl;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.basic.utils.CollHelper;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.manager.user.BaseOrgManager;
import com.dalio.cloud.base.mapper.user.BaseOrgMapper;
import com.dalio.cloud.common.cache.base.user.OrgCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * 通用业务实现类
 * 组织
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 * @create [2021-10-18] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseOrgManagerImpl extends SuperCacheManagerImpl<BaseOrgMapper, BaseOrg> implements BaseOrgManager {

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new OrgCacheKeyBuilder();
    }

    @Override
    @Transactional(readOnly = true)

    public Map<Serializable, Object> findByIds(Set<Serializable> params) {
        if (CollUtil.isEmpty(params)) {
            return Collections.emptyMap();
        }
        Set<Serializable> ids = new HashSet<>();
        params.forEach(item -> {
            if (item instanceof Collection tempItem) {
                ids.addAll(tempItem);
            } else {
                ids.add(item);
            }
        });

        List<BaseOrg> list = findByIds(ids, null);

        return CollHelper.uniqueIndex(list.stream().filter(Objects::nonNull).toList(), BaseOrg::getId, BaseOrg::getName);
    }

}
