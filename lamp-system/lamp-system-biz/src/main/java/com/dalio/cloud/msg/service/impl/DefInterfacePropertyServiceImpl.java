package com.dalio.cloud.msg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import com.dalio.cloud.msg.manager.DefInterfacePropertyManager;
import com.dalio.cloud.msg.service.DefInterfacePropertyService;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertyBatchSaveVO;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertySaveVO;
import com.dalio.cloud.msg.vo.update.DefInterfacePropertyUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务实现类
 * 接口属性
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DefInterfacePropertyServiceImpl extends SuperServiceImpl<DefInterfacePropertyManager, Long, DefInterfaceProperty> implements DefInterfacePropertyService {
    @Override
    public Map<String, Object> listByInterfaceId(Long id) {
        return superManager.listByInterfaceId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSave(DefInterfacePropertyBatchSaveVO saveVO) {
        List<DefInterfacePropertySaveVO> insertRecords = saveVO.getInsertRecords();
        List<DefInterfacePropertyUpdateVO> updateRecords = saveVO.getUpdateRecords();
        List<DefInterfacePropertyUpdateVO> removeRecords = saveVO.getRemoveRecords();
        List<DefInterfacePropertyUpdateVO> pendingRecords = saveVO.getPendingRecords();
        Set<String> insertKeys = insertRecords.stream().map(DefInterfacePropertySaveVO::getKey).collect(Collectors.toSet());
        Set<String> updateKeys = updateRecords.stream().map(DefInterfacePropertyUpdateVO::getKey).collect(Collectors.toSet());
        if (insertKeys.size() != insertRecords.size() || updateKeys.size() != updateRecords.size() || !Collections.disjoint(insertKeys, updateKeys)) {
            throw BizException.wrap("参数键重复");
        }

        List<Long> removeIdList = removeRecords.stream().map(DefInterfacePropertyUpdateVO::getId).toList();
        superManager.removeByIds(removeIdList);
        List<Long> pendingList = pendingRecords.stream().map(DefInterfacePropertyUpdateVO::getId).toList();
        superManager.removeByIds(pendingList);

        List<DefInterfaceProperty> updateList = BeanUtil.copyToList(updateRecords, DefInterfaceProperty.class);
        superManager.updateBatchById(updateList);

        List<DefInterfaceProperty> saveList = BeanUtil.copyToList(insertRecords, DefInterfaceProperty.class);
        superManager.saveBatch(saveList);

        return true;
    }
}


