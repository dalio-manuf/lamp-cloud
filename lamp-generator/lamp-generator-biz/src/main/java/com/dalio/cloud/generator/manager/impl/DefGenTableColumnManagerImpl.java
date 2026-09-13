package com.dalio.cloud.generator.manager.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.manager.DefGenTableColumnManager;
import com.dalio.cloud.generator.mapper.DefGenTableColumnMapper;

import java.util.Collection;

/**
 * <p>
 * 通用业务实现类
 * 代码生成字段
 * </p>
 *
 * @author admin
 * @date 2022-03-01
 * @create [2022-03-01] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefGenTableColumnManagerImpl extends SuperManagerImpl<DefGenTableColumnMapper, DefGenTableColumn> implements DefGenTableColumnManager {
    @Override
    public boolean removeByTableIds(Collection<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return false;
        }
        return remove(Wrappers.<DefGenTableColumn>lambdaQuery().in(
                DefGenTableColumn::getTableId, idList
        ));
    }
}
