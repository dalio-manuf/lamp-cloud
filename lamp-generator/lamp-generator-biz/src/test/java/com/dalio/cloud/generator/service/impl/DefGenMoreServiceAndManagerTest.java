package com.dalio.cloud.generator.service.impl;

import cn.hutool.db.meta.Column;
import cn.hutool.db.meta.Table;
import com.baidu.fsg.uid.UidGenerator;
import com.dalio.basic.database.properties.DatabaseProperties;
import com.dalio.cloud.generator.config.GeneratorConfig;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.manager.DefGenTableColumnManager;
import com.dalio.cloud.generator.manager.DefGenTableManager;
import com.dalio.cloud.generator.manager.impl.DefGenTableColumnManagerImpl;
import com.dalio.cloud.generator.mapper.DefGenTableColumnMapper;
import com.dalio.cloud.generator.vo.save.DefGenTableImportVO;
import com.dalio.cloud.test.entity.DefGenTestSimple;
import com.dalio.cloud.test.entity.DefGenTestTree;
import com.dalio.cloud.test.manager.DefGenTestSimpleManager;
import com.dalio.cloud.test.manager.DefGenTestTreeManager;
import com.dalio.cloud.test.manager.impl.DefGenTestSimpleManagerImpl;
import com.dalio.cloud.test.mapper.DefGenTestSimpleMapper;
import com.dalio.cloud.test.service.impl.DefGenTestSimpleServiceImpl;
import com.dalio.cloud.test.service.impl.DefGenTestTreeServiceImpl;
import com.dalio.cloud.test.vo.query.DefGenTestTreePageQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefGenMoreServiceAndManagerTest {

    @Test
    @DisplayName("测试 importTable 和 syncField")
    void testImportTableAndSyncField() {
        DefGenTableColumnManager columnManager = mock(DefGenTableColumnManager.class);
        DefGenTableManager tableManager = mock(DefGenTableManager.class);
        GeneratorConfig config = new GeneratorConfig();
        DatabaseProperties dbProps = new DatabaseProperties();
        UidGenerator uidGen = mock(UidGenerator.class);

        DefGenTableServiceImpl service = new DefGenTableServiceImpl(columnManager, config, dbProps, uidGen);
        ReflectionTestUtils.setField(service, "superManager", tableManager);

        DataSource ds = mock(DataSource.class);
        when(tableManager.getDs(anyLong())).thenReturn(ds);
        when(tableManager.getDbType()).thenReturn(com.baomidou.mybatisplus.annotation.DbType.MYSQL);

        DefGenTable genTable = new DefGenTable();
        genTable.setId(10L);
        genTable.setName("test_table");
        genTable.setDsId(1L);
        when(tableManager.getById(anyLong())).thenReturn(genTable);
        when(tableManager.save(any(DefGenTable.class))).thenReturn(true);
        when(columnManager.saveBatch(any())).thenReturn(true);
        when(columnManager.remove(any())).thenReturn(true);

        try (MockedStatic<cn.hutool.db.meta.MetaUtil> metaUtil = Mockito.mockStatic(cn.hutool.db.meta.MetaUtil.class)) {
            Table table = Table.create("test_table");
            table.setComment("Test Table");
            Column col = new Column();
            col.setName("id");
            col.setType(4); // INTEGER
            col.setTypeName("INT");
            col.setComment("ID");
            col.setPk(true);
            table.setColumn(col);
            metaUtil.when(() -> cn.hutool.db.meta.MetaUtil.getTableMeta(any(DataSource.class), anyString())).thenReturn(table);

            DefGenTableImportVO importVO = new DefGenTableImportVO();
            importVO.setDsId(1L);
            importVO.setTableNames(Collections.singletonList("test_table"));

            Boolean res = service.importTable(importVO);
            assertTrue(res);

            service.syncField(10L);
        }
    }

    @Test
    @DisplayName("测试 DefGenTableColumnManagerImpl.removeByTableIds")
    void testDefGenTableColumnManagerImpl() {
        DefGenTableColumnManagerImpl manager = new DefGenTableColumnManagerImpl();
        DefGenTableColumnMapper mapper = mock(DefGenTableColumnMapper.class);
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        assertFalse(manager.removeByTableIds(null));
        assertFalse(manager.removeByTableIds(Collections.emptyList()));

        when(mapper.delete(any())).thenReturn(1);
        assertTrue(manager.removeByTableIds(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("测试 DefGenTestTreeServiceImpl 与 DefGenTestSimpleServiceImpl")
    void testTestServicesAndManagers() {
        DefGenTestTreeServiceImpl treeService = new DefGenTestTreeServiceImpl();
        DefGenTestTreeManager treeManager = mock(DefGenTestTreeManager.class);
        ReflectionTestUtils.setField(treeService, "superManager", treeManager);

        DefGenTestTree node = new DefGenTestTree();
        node.setId(1L);
        node.setParentId(0L);
        node.setLabel("root");
        when(treeManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(node));

        List<DefGenTestTree> trees = treeService.findTree(new DefGenTestTreePageQuery());
        assertNotNull(trees);

        DefGenTestSimpleServiceImpl simpleService = new DefGenTestSimpleServiceImpl();
        DefGenTestSimpleManager simpleManager = mock(DefGenTestSimpleManager.class);
        ReflectionTestUtils.setField(simpleService, "superManager", simpleManager);
        assertNotNull(simpleService);

        DefGenTestSimpleManagerImpl simpleManagerImpl = new DefGenTestSimpleManagerImpl();
        DefGenTestSimpleMapper simpleMapper = mock(DefGenTestSimpleMapper.class);
        ReflectionTestUtils.setField(simpleManagerImpl, "baseMapper", simpleMapper);
        assertNotNull(simpleManagerImpl);

        com.dalio.cloud.test.manager.impl.DefGenTestTreeManagerImpl treeManagerImpl = new com.dalio.cloud.test.manager.impl.DefGenTestTreeManagerImpl();
        com.dalio.cloud.test.mapper.DefGenTestTreeMapper treeMapper = mock(com.dalio.cloud.test.mapper.DefGenTestTreeMapper.class);
        ReflectionTestUtils.setField(treeManagerImpl, "baseMapper", treeMapper);
        assertNotNull(treeManagerImpl);
    }
}
