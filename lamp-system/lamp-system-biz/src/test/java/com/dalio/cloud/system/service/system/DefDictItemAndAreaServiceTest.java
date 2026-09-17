package com.dalio.cloud.system.service.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.cache.repository.CachePlusOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.system.entity.system.DefArea;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefAreaManager;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.service.system.impl.DefAreaServiceImpl;
import com.dalio.cloud.system.service.system.impl.DefDictItemServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DefDictItemServiceImpl 和 DefAreaServiceImpl 单元测试
 */
class DefDictItemAndAreaServiceTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefDict.class);
        TableInfoHelper.initTableInfo(assistant, DefArea.class);
    }

    // ── DefDictItemService ────────────────────────────────────────────────────

    @Test
    @DisplayName("测试 checkItemByKey 参数校验 - 空 key 抛异常")
    void testCheckItemByKeyEmptyKey() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);

        DefDictItemServiceImpl service = new DefDictItemServiceImpl(cachePlusOps);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        assertThrows(ArgumentException.class, () -> service.checkItemByKey("", 1L, null));
        assertThrows(ArgumentException.class, () -> service.checkItemByKey(null, 1L, null));
    }

    @Test
    @DisplayName("测试 checkItemByKey 参数校验 - dictId 为空抛异常")
    void testCheckItemByKeyNullDictId() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);

        DefDictItemServiceImpl service = new DefDictItemServiceImpl(cachePlusOps);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        assertThrows(ArgumentException.class, () -> service.checkItemByKey("MALE", null, null));
    }

    @Test
    @DisplayName("测试 checkItemByKey 正常校验逻辑")
    void testCheckItemByKeyNormal() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);

        DefDictItemServiceImpl service = new DefDictItemServiceImpl(cachePlusOps);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        // 存在重复
        when(dictManager.count(any())).thenReturn(1L);
        assertTrue(service.checkItemByKey("MALE", 10L, null));

        // 不存在重复
        when(dictManager.count(any())).thenReturn(0L);
        assertFalse(service.checkItemByKey("FEMALE", 10L, 99L));
    }

    @Test
    @DisplayName("测试 DefDictItemServiceImpl save, updateById, removeByIds")
    void testDictItemSaveUpdateRemove() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);

        DefDictItemServiceImpl service = new DefDictItemServiceImpl(cachePlusOps);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        // 1. save - key 已存在报错
        com.dalio.cloud.system.vo.save.system.DefDictItemSaveVO saveVO = new com.dalio.cloud.system.vo.save.system.DefDictItemSaveVO();
        saveVO.setKey("1");
        saveVO.setParentId(10L);
        when(dictManager.count(any())).thenReturn(1L);
        assertThrows(ArgumentException.class, () -> service.save(saveVO));

        // 2. save - parent 不存在报错
        when(dictManager.count(any())).thenReturn(0L);
        when(dictManager.getById(10L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.save(saveVO));

        // 3. save - 成功保存
        DefDict parent = new DefDict();
        parent.setId(10L);
        parent.setKey("SEX");
        when(dictManager.getById(10L)).thenReturn(parent);
        when(dictManager.save(any(DefDict.class))).thenReturn(true);
        DefDict saved = service.save(saveVO);
        assertNotNull(saved);
        assertEquals("SEX", saved.getParentKey());
        verify(cachePlusOps).hSet(any(), any());

        // 4. updateById - old 不存在
        com.dalio.cloud.system.vo.update.system.DefDictItemUpdateVO updateVO = new com.dalio.cloud.system.vo.update.system.DefDictItemUpdateVO();
        updateVO.setId(101L);
        updateVO.setParentId(10L);
        updateVO.setKey("1_NEW");
        when(dictManager.getById(101L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.updateById(updateVO));

        // 5. updateById - 成功更新
        DefDict oldItem = new DefDict();
        oldItem.setId(101L);
        oldItem.setKey("1_OLD");
        when(dictManager.getById(101L)).thenReturn(oldItem);
        when(dictManager.updateById(any(DefDict.class))).thenReturn(true);

        DefDict updated = service.updateById(updateVO);
        assertNotNull(updated);
        verify(cachePlusOps).hDel(any());
        verify(cachePlusOps, atLeast(2)).hSet(any(), any());

        // 6. removeByIds
        when(dictManager.removeItemByIds(anyList())).thenReturn(true);
        assertTrue(service.removeByIds(List.of(101L)));
    }

    // ── DefAreaService ────────────────────────────────────────────────────────

    @Test
    @DisplayName("测试 check 地区编码唯一性校验 - 空编码抛异常")
    void testAreaCheckEmptyCode() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        assertThrows(ArgumentException.class, () -> service.check("", null));
        assertThrows(ArgumentException.class, () -> service.check(null, null));
    }

    @Test
    @DisplayName("测试 check 地区编码唯一性校验 - 正常查询")
    void testAreaCheckNormal() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        when(areaManager.count(any())).thenReturn(1L);
        assertTrue(service.check("110000", null));

        when(areaManager.count(any())).thenReturn(0L);
        assertFalse(service.check("120000", 5L));
    }

    @Test
    @DisplayName("测试 saveBefore, updateBefore, updateAllBefore 树路径填充")
    void testAreaSavesAndUpdates() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        when(areaManager.count(any())).thenReturn(0L);

        // 1. 根地区保存
        com.dalio.cloud.system.vo.save.system.DefAreaSaveVO saveVO = new com.dalio.cloud.system.vo.save.system.DefAreaSaveVO();
        saveVO.setCode("110000");
        saveVO.setName("北京市");
        saveVO.setParentId(0L);
        DefArea rootArea = ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO);
        assertNotNull(rootArea);
        assertEquals(0L, rootArea.getParentId());
        assertEquals(com.dalio.cloud.common.constant.DefValConstants.TREE_GRADE, rootArea.getTreeGrade());

        // 2. 子地区保存 - 父地区不存在
        saveVO.setParentId(100L);
        when(areaManager.getById(100L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO));

        // 3. 子地区保存 - 正常
        DefArea parentArea = new DefArea();
        parentArea.setId(100L);
        parentArea.setTreeGrade(1);
        parentArea.setTreePath("0,");
        when(areaManager.getById(100L)).thenReturn(parentArea);
        DefArea childArea = ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO);
        assertNotNull(childArea);
        assertEquals(2, childArea.getTreeGrade());

        // 4. updateBefore & updateAllBefore
        com.dalio.cloud.system.vo.update.system.DefAreaUpdateVO updateVO = new com.dalio.cloud.system.vo.update.system.DefAreaUpdateVO();
        updateVO.setId(200L);
        updateVO.setCode("110100");
        updateVO.setName("市辖区");
        updateVO.setParentId(100L);
        DefArea upArea = ReflectionTestUtils.invokeMethod(service, "updateBefore", updateVO);
        assertNotNull(upArea);

        DefArea upAllArea = service.updateAllBefore(updateVO);
        assertNotNull(upAllArea);
    }

    @Test
    @DisplayName("测试 findTree 含有搜索关键字时路径补全")
    void testFindTreeWithSearch() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        // 1. 搜索无结果
        when(areaManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.emptyList());
        com.dalio.cloud.system.vo.query.system.DefAreaPageQuery query = new com.dalio.cloud.system.vo.query.system.DefAreaPageQuery();
        query.setName("不存在地区");
        List<DefArea> emptyResult = service.findTree(query);
        assertTrue(emptyResult.isEmpty());

        // 2. 搜索有结果且关联父节点
        DefArea searchArea = new DefArea();
        searchArea.setId(200L);
        searchArea.setParentId(100L);
        searchArea.setName("朝阳区");
        searchArea.setTreePath("0,100,");

        DefArea parentArea = new DefArea();
        parentArea.setId(100L);
        parentArea.setParentId(0L);
        parentArea.setName("北京市");
        parentArea.setTreePath("0,");

        when(areaManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class)))
                .thenReturn(new ArrayList<>(List.of(searchArea)))
                .thenReturn(List.of(parentArea));
        List<DefArea> res = service.findTree(query);
        assertNotNull(res);

        // 3. 空查询条件，返回全部树
        when(areaManager.list()).thenReturn(List.of(parentArea));
        List<DefArea> result2 = service.findTree(null);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("测试 findLazyList 与 downloadJson 树导出")
    void testFindLazyListAndDownloadJson() throws Exception {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        DefArea area1 = new DefArea();
        area1.setId(200L);
        area1.setParentId(100L);
        area1.setName("北京市");

        when(areaManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(area1));
        List<DefArea> result = service.findLazyList(100L);
        assertEquals(1, result.size());
        assertEquals("北京市", result.get(0).getName());

        // downloadJson
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        service.downloadJson(2, request, response);
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("北京市"));
    }
}
