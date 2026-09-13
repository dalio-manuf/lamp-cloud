package com.dalio.cloud.system.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.cache.repository.CachePlusOps;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.model.cache.CacheHashKey;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.common.cache.tenant.base.DictCacheKeyBuilder;
import com.dalio.cloud.model.enumeration.system.DictClassifyEnum;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.service.system.DefDictItemService;
import com.dalio.cloud.system.vo.save.system.DefDictItemSaveVO;
import com.dalio.cloud.system.vo.update.system.DefDictItemUpdateVO;

import java.util.Collection;

/**
 * <p>
 * 业务实现类
 * 字典
 * </p>
 *
 * @author admin
 * @date 2021-10-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class DefDictItemServiceImpl extends SuperServiceImpl<DefDictManager, Long, DefDict> implements DefDictItemService {

    private final CachePlusOps cachePlusOps;


    @Override
    public boolean checkItemByKey(String key, Long dictId, Long id) {
        ArgumentAssert.notEmpty(key, "请填写字典项标识");
        ArgumentAssert.notNull(dictId, "字典不能为空");
        return superManager.count(Wraps.<DefDict>lbQ().eq(DefDict::getKey, key)
                .eq(DefDict::getParentId, dictId).ne(DefDict::getId, id)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <SaveVO> DefDict save(SaveVO saveVO) {
        DefDictItemSaveVO itemSaveVO = (DefDictItemSaveVO) saveVO;
        DefDict model = BeanUtil.toBean(itemSaveVO, DefDict.class);
        ArgumentAssert.isFalse(checkItemByKey(model.getKey(), model.getParentId(), null), "字典[{}]已经存在，请勿重复创建", model.getKey());
        DefDict parent = getById(model.getParentId());
        ArgumentAssert.notNull(parent, "字典不存在");
        model.setParentKey(parent.getKey());
        model.setDictGroup(parent.getDictGroup());
        model.setDataType(parent.getDataType());
        model.setClassify(parent.getClassify());
        model.setClassify(DictClassifyEnum.SYSTEM.getCode());
        superManager.save(model);
        CacheHashKey hashKey = DictCacheKeyBuilder.builder(model.getParentKey(), model.getKey());
        cachePlusOps.hSet(hashKey, model);
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <UpdateVO> DefDict updateById(UpdateVO updateVO) {
        DefDictItemUpdateVO itemUpdateVO = (DefDictItemUpdateVO) updateVO;
        DefDict model = BeanUtil.toBean(itemUpdateVO, DefDict.class);
        DefDict old = getById(model.getId());
        ArgumentAssert.notNull(old, "您要修改的字典项不存在或已被删除！");
        DefDict parent = getById(model.getParentId());
        ArgumentAssert.notNull(parent, "您要修改的字典不存在或已被删除！");
        model.setParentKey(parent.getKey());
        model.setDictGroup(parent.getDictGroup());
        model.setDataType(parent.getDataType());
        model.setClassify(parent.getClassify());
        superManager.updateById(model);

        // 淘汰旧缓存
        CacheHashKey oldHashKey = DictCacheKeyBuilder.builder(parent.getKey(), old.getKey());
        cachePlusOps.hDel(oldHashKey);
        // 设置新缓存
        CacheHashKey hashKey = DictCacheKeyBuilder.builder(parent.getKey(), model.getKey());
        cachePlusOps.hSet(hashKey, model);
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> idList) {
        return superManager.removeItemByIds(idList);
    }
}
