package com.dalio.cloud.generator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.BeanPlusUtil;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.manager.DefGenTableColumnManager;
import com.dalio.cloud.generator.manager.DefGenTableManager;
import com.dalio.cloud.generator.service.DefGenTableColumnService;
import com.dalio.cloud.generator.utils.GenUtils;
import com.dalio.cloud.generator.vo.query.DefGenTableColumnPageQuery;
import com.dalio.cloud.generator.vo.result.DefGenTableColumnResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * <p>
 * 业务实现类
 * 代码生成字段
 * </p>
 *
 * @author admin
 * @date 2022-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefGenTableColumnServiceImpl extends SuperServiceImpl<DefGenTableColumnManager, Long, DefGenTableColumn> implements DefGenTableColumnService {
    private final DefGenTableManager defGenTableManager;
    private final GeneratorConfig generatorConfig;


    @Override
    public IPage<DefGenTableColumnResultVO> pageColumn(PageParams<DefGenTableColumnPageQuery> params) {
        IPage<DefGenTableColumn> page = params.buildPage();
        DefGenTableColumnPageQuery model = params.getModel();
        DefGenTableColumn column = BeanUtil.toBean(model, DefGenTableColumn.class);
        superManager.page(page, Wraps.lbQ(column));
        return BeanPlusUtil.toBeanPage(page, DefGenTableColumnResultVO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncField(Long tableId, Long id) {
        DefGenTable genTable = defGenTableManager.getById(tableId);
        DefGenTableColumn genTableColumn = getById(id);
        ArgumentAssert.notNull(genTable, "请先选择需要同步的表");
        ArgumentAssert.notNull(genTableColumn, "请先选择需要同步的字段");
        DataSource ds = defGenTableManager.getDs(genTable.getDsId());

        Table tableMeta = MetaUtil.getTableMeta(ds, genTable.getName());
        if (tableMeta == null || CollUtil.isEmpty(tableMeta.getColumns())) {
            throw BizException.wrap("未获取到表结构信息，请确保该表在数据源中存在且至少包含1个字段");
        }
        for (Column column : tableMeta.getColumns()) {
            if (genTableColumn.getName().equals(column.getName())) {
                DefGenTableColumn tableColumn = GenUtils.initColumnField(generatorConfig, defGenTableManager.getDbType(), genTable, column);
                if (tableColumn != null) {
                    tableColumn.setId(id);
                    tableColumn.setTableId(tableId);
                    superManager.updateById(tableColumn);
                    break;
                }
            }
        }
        return true;
    }
}
