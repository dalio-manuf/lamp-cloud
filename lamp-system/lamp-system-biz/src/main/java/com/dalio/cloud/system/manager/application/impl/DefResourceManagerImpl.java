package com.dalio.cloud.system.manager.application.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.dalio.basic.base.manager.impl.SuperCacheManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.TreeUtil;
import com.dalio.cloud.common.cache.tenant.application.ResourceCacheKeyBuilder;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.manager.application.DefResourceManager;
import com.dalio.cloud.system.mapper.application.DefResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * 应用管理
 *
 * @author admin
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [admin] [初始创建]
 */
@RequiredArgsConstructor
@Service
public class DefResourceManagerImpl extends SuperCacheManagerImpl<DefResourceMapper, DefResource> implements DefResourceManager {


    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new ResourceCacheKeyBuilder();
    }

    @Override
    public List<DefResource> findResourceListByApplicationId(List<Long> applicationIdList, final Collection<String> resourceTypeList) {
        if (CollUtil.isEmpty(applicationIdList)) {
            // 查询所有的菜单
            return super.list(Wraps.<DefResource>lbQ().eq(DefResource::getState, true).orderByAsc(DefResource::getSortValue));
        } else {
            // 新方法
            List<Long> resourceIdSet = addApplicationResourceIdList(applicationIdList);
            return findByIdsAndType(resourceIdSet, resourceTypeList);
        }
    }

    private List<Long> addApplicationResourceIdList(List<Long> applicationIdList) {
        LbQueryWrap<DefResource> wrap = Wraps.<DefResource>lbQ().select(DefResource::getId).in(DefResource::getApplicationId, applicationIdList).eq(DefResource::getState, true);
        return super.listObjs(wrap, Convert::toLong);
    }

    @Override
    public List<DefResource> findByIdsAndType(Collection<? extends Serializable> idList, Collection<String> types) {
        List<DefResource> list = findByIds(idList, null);
        final Set<String> typeSet = CollUtil.isNotEmpty(types) ? (types instanceof Set ? (Set<String>) types : new HashSet<>(types)) : null;
        return list.stream()
                // 过滤数据状态，防止 NPE
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getState()))
                .filter(item -> typeSet == null || typeSet.contains(item.getResourceType()))
                // 按sortValue排序，null排在最后
                .sorted(Comparator.comparing(DefResource::getSortValue, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    @Override
    public List<DefResource> findByApplicationId(List<Long> applicationIds) {
        ArgumentAssert.notEmpty(applicationIds, "applicationIds 不能为空");
        return list(Wraps.<DefResource>lbQ().in(DefResource::getApplicationId, applicationIds).orderByAsc(DefResource::getSortValue));
    }

    @Override
    public List<DefResource> findChildrenByParentId(Long parentId) {
        ArgumentAssert.notNull(parentId, "parentId 不能为空");
        return list(Wraps.<DefResource>lbQ().in(DefResource::getParentId, parentId).orderByAsc(DefResource::getSortValue));
    }

    @Override
    public int deleteRoleResourceRelByResourceId(List<Long> resourceIds) {
        return baseMapper.deleteRoleResourceRelByResourceId(resourceIds);
    }

    @Override
    public List<DefResource> findAllChildrenByParentId(Long parentId) {
        ArgumentAssert.notNull(parentId, "parentId 不能为空");
        return list(Wraps.<DefResource>lbQ().like(DefResource::getTreePath, TreeUtil.buildTreePath(parentId)).orderByAsc(DefResource::getSortValue));
    }


}
