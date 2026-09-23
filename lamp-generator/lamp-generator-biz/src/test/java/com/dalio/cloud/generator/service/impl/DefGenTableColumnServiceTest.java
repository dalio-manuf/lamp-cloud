package com.dalio.cloud.generator.service.impl;

import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dalio.basic.base.request.PageParams;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.entity.DefGenTableColumn;
import com.dalio.cloud.generator.manager.DefGenTableColumnManager;
import com.dalio.cloud.generator.manager.DefGenTableManager;
import com.dalio.cloud.generator.vo.query.DefGenTableColumnPageQuery;
import com.dalio.cloud.generator.vo.result.DefGenTableColumnResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefGenTableColumnServiceTest {

    @Test
    @DisplayName("测试 pageColumn 分页查询")
    void testPageColumn() {
        DefGenTableManager tableManager = mock(DefGenTableManager.class);
        GeneratorConfig config = new GeneratorConfig();
        DefGenTableColumnManager columnManager = mock(DefGenTableColumnManager.class);

        DefGenTableColumnServiceImpl service = new DefGenTableColumnServiceImpl(tableManager, config);
        ReflectionTestUtils.setField(service, "superManager", columnManager);

        PageParams<DefGenTableColumnPageQuery> params = new PageParams<>();
        params.setSize(10);
        params.setCurrent(1);
        DefGenTableColumnPageQuery query = new DefGenTableColumnPageQuery();
        query.setName("test_col");
        params.setModel(query);

        doAnswer(invocation -> {
            IPage<DefGenTableColumn> argPage = invocation.getArgument(0);
            DefGenTableColumn col = new DefGenTableColumn();
            col.setId(1L);
            col.setName("test_col");
            argPage.setRecords(Collections.singletonList(col));
            return argPage;
        }).when(columnManager).page(any(IPage.class), any(Wrapper.class));

        IPage<DefGenTableColumnResultVO> voPage = service.pageColumn(params);
        assertNotNull(voPage);
        assertEquals(1, voPage.getRecords().size());
        assertEquals("test_col", voPage.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("测试 syncField 同步字段结构")
    void testSyncField() {
        DefGenTableManager tableManager = mock(DefGenTableManager.class);
        GeneratorConfig config = new GeneratorConfig();
        DefGenTableColumnManager columnManager = mock(DefGenTableColumnManager.class);

        DefGenTableColumnServiceImpl service = spy(new DefGenTableColumnServiceImpl(tableManager, config));
        ReflectionTestUtils.setField(service, "superManager", columnManager);

        DefGenTable table = new DefGenTable();
        table.setId(10L);
        table.setName("test_table");
        table.setDsId(100L);

        DefGenTableColumn col = new DefGenTableColumn();
        col.setId(20L);
        col.setName("test_col");

        when(tableManager.getById(10L)).thenReturn(table);
        doReturn(col).when(service).getById(20L);

        DataSource ds = mock(DataSource.class);
        when(tableManager.getDs(100L)).thenReturn(ds);
        when(tableManager.getDbType()).thenReturn(DbType.MYSQL);

        try (MockedStatic<MetaUtil> metaUtilMockedStatic = mockStatic(MetaUtil.class)) {
            // 1. 无表结构信息
            metaUtilMockedStatic.when(() -> MetaUtil.getTableMeta(ds, "test_table")).thenReturn(null);
            assertThrows(RuntimeException.class, () -> service.syncField(10L, 20L));

            // 2. 表结构存在，但空列
            Table tableMeta = Table.create("test_table");
            metaUtilMockedStatic.when(() -> MetaUtil.getTableMeta(ds, "test_table")).thenReturn(tableMeta);
            assertThrows(RuntimeException.class, () -> service.syncField(10L, 20L));

            // 3. 有列，执行同步
            Column c = new Column();
            c.setName("test_col");
            c.setType(12); // VARCHAR
            c.setComment("test col");
            tableMeta.setColumn(c);

            metaUtilMockedStatic.when(() -> MetaUtil.getTableMeta(ds, "test_table")).thenReturn(tableMeta);
            when(columnManager.updateById(any(DefGenTableColumn.class))).thenReturn(true);

            Boolean result = service.syncField(10L, 20L);
            assertTrue(result);
            verify(columnManager, times(1)).updateById(any(DefGenTableColumn.class));
        }
    }
}
