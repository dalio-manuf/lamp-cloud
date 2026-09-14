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
    @DisplayName("测试 findTree 含有搜索关键字时路径补全")
    void testFindTreeWithSearch() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        // 1. 搜索无结果
        when(areaManager.list(any())).thenReturn(Collections.emptyList());
        com.dalio.cloud.system.vo.query.system.DefAreaPageQuery query = new com.dalio.cloud.system.vo.query.system.DefAreaPageQuery();
        query.setName("不存在地区");
        List<DefArea> emptyResult = service.findTree(query);
        assertTrue(emptyResult.isEmpty());

        // 2. 空查询条件，返回全部树
        List<DefArea> result2 = service.findTree(null);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("测试 findLazyList 懒加载子节点")
    void testFindLazyList() {
        DefAreaManager areaManager = Mockito.mock(DefAreaManager.class);
        DefAreaServiceImpl service = new DefAreaServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", areaManager);

        DefArea area1 = new DefArea();
        area1.setId(200L);
        area1.setParentId(100L);
        area1.setName("北京市");

        when(areaManager.list(any())).thenReturn(List.of(area1));
        List<DefArea> result = service.findLazyList(100L);
        assertEquals(1, result.size());
        assertEquals("北京市", result.get(0).getName());
    }
}
