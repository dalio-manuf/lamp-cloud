package com.dalio.cloud.system.service.system.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.cache.repository.CachePlusOps;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.model.cache.CacheHashKey;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.BeanPlusUtil;
import com.dalio.cloud.common.cache.tenant.base.DictCacheKeyBuilder;
import com.dalio.cloud.common.constant.DefValConstants;
import com.dalio.cloud.model.enumeration.system.DictClassifyEnum;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.service.system.DefDictService;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;
import com.dalio.cloud.system.vo.save.system.DefDictItemSaveVO;
import com.dalio.cloud.system.vo.save.system.DefDictSaveVO;
import com.dalio.cloud.system.vo.update.system.DefDictItemUpdateVO;
import com.dalio.cloud.system.vo.update.system.DefDictUpdateVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class DefDictServiceImpl extends SuperServiceImpl<DefDictManager, Long, DefDict> implements DefDictService {

    private final CachePlusOps cachePlusOps;
    private final UidGenerator uidGenerator;

    @Override
    public boolean checkByKey(String key, Long id) {
        ArgumentAssert.notEmpty(key, "请填写字典标识");
        return superManager.count(Wraps.<DefDict>lbQ().eq(DefDict::getKey, key)
                .eq(DefDict::getParentId, DefValConstants.PARENT_ID).ne(DefDict::getId, id)) > 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public <SaveVO> DefDict save(SaveVO saveVO) {
        DefDictSaveVO dictSaveVO = (DefDictSaveVO) saveVO;
        ArgumentAssert.isFalse(checkByKey(dictSaveVO.getKey(), null), "字典标识已存在");
        DefDict dict = BeanPlusUtil.toBean(dictSaveVO, DefDict.class);
        dict.setClassify(DictClassifyEnum.SYSTEM.getCode());
        dict.setParentId(DefValConstants.PARENT_ID);
        superManager.save(dict);

        saveItem(dictSaveVO.getInsertList(), dict);
        return dict;
    }

    private void saveItem(List<DefDictItemSaveVO> insertList, DefDict dict) {
        if (CollUtil.isNotEmpty(insertList)) {
            List<DefDict> itemList = new ArrayList<>();
            insertList.forEach(insert -> {
                DefDict item = new DefDict();
                BeanPlusUtil.copyProperties(insert, item);
                item.setParentId(dict.getId());
                item.setParentKey(dict.getKey());
                item.setClassify(DictClassifyEnum.SYSTEM.getCode());
                itemList.add(item);
            });
            superManager.saveBatch(itemList);
            itemList.forEach(item -> {
                CacheHashKey hashKey = DictCacheKeyBuilder.builder(item.getParentKey(), item.getKey());
                cachePlusOps.hSet(hashKey, item);
            });
        }
    }

    private void updateItem(List<DefDictItemUpdateVO> updateInsert, DefDict dict, DefDict old) {
        if (CollUtil.isNotEmpty(updateInsert)) {
            List<DefDict> itemList = new ArrayList<>(updateInsert.size());
            updateInsert.forEach(insert -> {
                DefDict item = new DefDict();
                BeanPlusUtil.copyProperties(insert, item);
                item.setParentId(dict.getId());
                item.setParentKey(dict.getKey());
                item.setClassify(DictClassifyEnum.SYSTEM.getCode());
                itemList.add(item);
            });
            superManager.updateBatchById(itemList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDict(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        List<DefDict> list = superManager.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        // 删除 字典条目
        superManager.remove(Wraps.<DefDict>lbQ().in(DefDict::getParentId, ids));

//        删除字典
        boolean flag = removeByIds(ids);
        CacheHashKey[] typeKeys = list.stream().map(type -> DictCacheKeyBuilder.builder(type.getKey())).toArray(CacheHashKey[]::new);
        cachePlusOps.del(typeKeys);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <UpdateVO> DefDict updateById(UpdateVO updateVO) {
        DefDictUpdateVO dictUpdateVO = (DefDictUpdateVO) updateVO;
        ArgumentAssert.isFalse(checkByKey(dictUpdateVO.getKey(), dictUpdateVO.getId()), "标识【{}】重复", dictUpdateVO.getKey());

        DefDict old = getById(dictUpdateVO.getId());

        DefDict dict = BeanPlusUtil.toBean(dictUpdateVO, DefDict.class);
        dict.setParentId(DefValConstants.PARENT_ID);
        dict.setClassify(DictClassifyEnum.SYSTEM.getCode());
        superManager.updateById(dict);

        LambdaUpdateWrapper<DefDict> updateWrapper = Wrappers.lambdaUpdate(DefDict.class)
                .eq(DefDict::getParentId, dict.getId())
                .set(DefDict::getParentKey, dict.getKey())
                .set(DefDict::getDictGroup, dict.getDictGroup())
                .set(DefDict::getDataType, dict.getDataType());
        superManager.update(updateWrapper);

        // 淘汰当前字典标识缓存，若标识发生变更，同步淘汰旧标识缓存
        CacheKey hashKey = DictCacheKeyBuilder.builder(dict.getKey());
        cachePlusOps.del(hashKey);
        if (old != null && !Objects.equals(old.getKey(), dict.getKey())) {
            cachePlusOps.del(DictCacheKeyBuilder.builder(old.getKey()));
        }

        superManager.removeItemByIds(dictUpdateVO.getDeleteList());
        updateItem(dictUpdateVO.getUpdateList(), dict, old);
        saveItem(dictUpdateVO.getInsertList(), dict);

        return dict;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefDict copy(Long id) {
        DefDict old = getById(id);
        ArgumentAssert.notNull(old, "字典不存在或已被删除，请刷新重试");

        CopyOptions copyOptions = CopyOptions.create()
                .setIgnoreProperties(DefDict::getId, DefDict::getCreatedTime, DefDict::getCreatedBy, DefDict::getUpdatedTime, DefDict::getUpdatedBy);
        DefDict dict = BeanPlusUtil.toBean(old, DefDict.class, copyOptions);
        dict.setId(null);
        dict.setKey(dict.getKey() + "_copy");
        dict.setParentId(DefValConstants.PARENT_ID);
        dict.setClassify(DictClassifyEnum.SYSTEM.getCode());
        superManager.save(dict);

        LbQueryWrap<DefDict> wrap = Wraps.<DefDict>lbQ().eq(DefDict::getParentId, old.getId());
        List<DefDict> itemList = superManager.list(wrap);
        itemList.forEach(item -> {
            item.setId(null);
            item.setParentId(dict.getId());
            item.setParentKey(dict.getKey());
            item.setCreatedTime(null);
            item.setCreatedBy(null);
            item.setUpdatedTime(null);
            item.setUpdatedBy(null);
        });
        superManager.saveBatch(itemList);
        return dict;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importDictByEnum(List<DefDictResultVO> list) {
        if (CollUtil.isEmpty(list)) {
            return true;
        }

        List<String> keyList = list.stream().map(DefDictResultVO::getKey).toList();
        List<DefDict> existDictList = super.list(Wraps.<DefDict>lbQ().eq(DefDict::getParentId, DefValConstants.PARENT_ID).in(DefDict::getKey, keyList));

        // 将已存在的字典按uniqKey分组，便于快速查找
        Map<String, DefDict> existingDictMap = existDictList.stream()
                .collect(Collectors.toMap(DefDict::getKey, dict -> dict, (k1, k2) -> k1));

        // 区分需要新增和更新的数据
        List<DefDict> toSave = new ArrayList<>();
        List<DefDict> toSaveDictItemList = new ArrayList<>();

        List<DefDict> toUpdate = new ArrayList<>();
        List<DefDict> toUpdateDictItemList = new ArrayList<>();

        for (DefDictResultVO vo : list) {
            String uniqKey = vo.getKey();
            // 检查是否已存在
            if (existingDictMap.containsKey(uniqKey)) {
                // 已存在则更新
                DefDict existingDict = existingDictMap.get(uniqKey);
                existingDict.setName(vo.getName());
                existingDict.setClassify(DictClassifyEnum.ENUM.getCode());
                toUpdate.add(existingDict);

                List<DefDictItemResultVO> itemList = vo.getItemList();
                if (CollUtil.isNotEmpty(itemList)) {

                    List<String> itemKeyList = itemList.stream().map(DefDictItemResultVO::getKey).toList();
                    // 已存在的字典项 list
                    List<DefDict> existDictItemList = superManager.list(Wraps.<DefDict>lbQ().eq(DefDict::getParentId, existingDict.getId()).in(DefDict::getKey, itemKeyList));
                    // 已存在的字典项 map
                    Map<String, DefDict> existingDictItemMap = existDictItemList.stream().collect(Collectors.toMap(DefDict::getKey, dict -> dict, (k1, k2) -> k1));

                    int weight = 0;
                    for (DefDictItemResultVO itemVo : itemList) {
                        String itemKey = itemVo.getKey();
                        if (existingDictItemMap.containsKey(itemKey)) {
                            // 修改
                            DefDict item = existingDictItemMap.get(itemKey);
                            item.setName(itemVo.getName());
                            item.setDataType(existingDict.getDataType());
                            item.setClassify(DictClassifyEnum.ENUM.getCode());

                            toUpdateDictItemList.add(item);
                        } else {
                            DefDict item = new DefDict();
                            item.setId(uidGenerator.getUid());
                            item.setParentId(existingDict.getId());
                            item.setParentKey(existingDict.getKey());
                            item.setKey(itemVo.getKey());
                            item.setName(itemVo.getName());
                            item.setState(true);
                            item.setSortValue(weight++);
                            item.setClassify(DictClassifyEnum.ENUM.getCode());
                            item.setDataType(existingDict.getDataType());
                            toSaveDictItemList.add(item);
                        }
                    }
                }

            } else {
                // 不存在则新增
                DefDict newDict = new DefDict();
                newDict.setId(uidGenerator.getUid());
                newDict.setKey(uniqKey);
                newDict.setParentId(DefValConstants.PARENT_ID);
                newDict.setRemark("枚举导入");
                newDict.setName(vo.getName());
                newDict.setState(true);
                newDict.setClassify(DictClassifyEnum.ENUM.getCode());
                newDict.setDictGroup(vo.getDictGroup());
                newDict.setDataType(vo.getDataType());
                toSave.add(newDict);

                List<DefDictItemResultVO> itemList = vo.getItemList();
                if (CollUtil.isNotEmpty(itemList)) {
                    int weight = 0;
                    for (DefDictItemResultVO itemVo : itemList) {
                        DefDict item = new DefDict();
                        item.setId(uidGenerator.getUid());
                        item.setParentId(newDict.getId());
                        item.setParentKey(newDict.getKey());
                        item.setKey(itemVo.getKey());
                        item.setName(itemVo.getName());
                        item.setState(true);
                        item.setSortValue(weight++);
                        item.setClassify(DictClassifyEnum.ENUM.getCode());
                        item.setDictGroup(vo.getDictGroup());
                        item.setDataType(newDict.getDataType());
                        toSaveDictItemList.add(item);
                    }
                }
            }
        }
        // 执行批量操作
        if (!toSave.isEmpty()) {
            saveBatch(toSave);
        }
        if (!toSaveDictItemList.isEmpty()) {
            superManager.saveBatch(toSaveDictItemList);
        }
        log.info("已经新增字典：{}条，字典项：{}条", toSave.size(), toSaveDictItemList.size());
        if (!toUpdate.isEmpty()) {
            superManager.updateBatchById(toUpdate);
        }
        if (!toUpdateDictItemList.isEmpty()) {
            superManager.updateBatchById(toUpdateDictItemList);
        }
        log.info("已经更新字典：{}条，字典项：{}条", toUpdate.size(), toUpdateDictItemList.size());

        // 淘汰缓存
        List<CacheKey> cacheKeyList = keyList.stream().map(DictCacheKeyBuilder::builder).toList();
        cachePlusOps.del(cacheKeyList);
        return true;
    }

    @Override
    public List<DefDict> findItemByDictId(Long id) {
        return list(Wraps.<DefDict>lbQ().eq(DefDict::getParentId, id));
    }
}
