package com.dalio.cloud.system.manager.system;

import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CachePlusOps;
import com.dalio.basic.echo.properties.EchoProperties;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.impl.DefDictManagerImpl;
import com.dalio.cloud.system.mapper.system.DefDictMapper;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DefDictManagerImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefDict.class);
    }

    @Test
    @DisplayName("测试 syncEnumToDict 同步枚举到字典")
    void testSyncEnumToDict() {
        DefDictMapper mapper = mock(DefDictMapper.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        EchoProperties echoProperties = new EchoProperties();
        UidGenerator uidGenerator = mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(9999L);

        DefDictManagerImpl manager = Mockito.spy(new DefDictManagerImpl(mapper, cachePlusOps, echoProperties, uidGenerator));
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        Option typeOpt = Option.builder().value("DATA_TYPE").label("数据类型").color("String").remark("String").build();
        Option itemOpt = Option.builder().value("VAL_1").label("值1").color("primary").build();
        Map<Option, List<Option>> enumMap = Map.of(typeOpt, List.of(itemOpt));

        // 1. 字典不存在新增
        doReturn(null).when(manager).getOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        doReturn(true).when(manager).save(any(DefDict.class));
        doReturn(true).when(manager).saveBatch(anyList());

        manager.syncEnumToDict(enumMap);
        verify(manager).save(any(DefDict.class));
        verify(manager).saveBatch(anyList());

        // 2. 字典已存在更新
        DefDict existing = new DefDict();
        existing.setId(10L);
        existing.setKey("DATA_TYPE");
        existing.setSortValue(10);
        doReturn(existing).when(manager).getOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        doReturn(true).when(manager).updateById(any(DefDict.class));

        manager.syncEnumToDict(enumMap);
        verify(manager).updateById(existing);
    }

    @Test
    @DisplayName("测试 findByIds 与 回调函数")
    void testFindByIds() {
        DefDictMapper mapper = mock(DefDictMapper.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        EchoProperties echoProperties = new EchoProperties();
        echoProperties.setDictSeparator("#");
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictManagerImpl manager = new DefDictManagerImpl(mapper, cachePlusOps, echoProperties, uidGenerator);
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 空集
        assertTrue(manager.findByIds(Collections.emptySet()).isEmpty());

        // 模拟 cachePlusOps.hGetAll 回调
        doAnswer(invocation -> {
            Function<Object, Map<String, DefDict>> fun = invocation.getArgument(1);
            Map<String, DefDict> loaded = fun.apply(null);
            Map<String, CacheResult<DefDict>> result = new HashMap<>();
            loaded.forEach((k, v) -> result.put(k, new CacheResult<DefDict>(k, v)));
            return result;
        }).when(cachePlusOps).hGetAll(any(), any());

        DefDict item = new DefDict();
        item.setKey("MALE");
        item.setName("男");
        when(mapper.selectList(any())).thenReturn(List.of(item));

        Map<Serializable, DefDict> map = manager.findByIds(Set.of("SEX"));
        assertEquals(1, map.size());
        assertTrue(map.containsKey("SEX#MALE"));
        assertEquals("男", map.get("SEX#MALE").getName());
    }

    @Test
    @DisplayName("测试 findDictMapItemListByKey 与 removeItemByIds")
    void testFindDictMapItemListByKeyAndRemove() {
        DefDictMapper mapper = mock(DefDictMapper.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        EchoProperties echoProperties = new EchoProperties();
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictManagerImpl manager = Mockito.spy(new DefDictManagerImpl(mapper, cachePlusOps, echoProperties, uidGenerator));
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 1. findDictMapItemListByKey 空与非空
        assertTrue(manager.findDictMapItemListByKey(Collections.emptyList()).isEmpty());

        DefDict item = new DefDict();
        item.setId(1L);
        item.setParentKey("SEX");
        item.setKey("1");
        item.setName("男");
        doReturn(List.of(item)).when(manager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        Map<String, List<DefDictItemResultVO>> res = manager.findDictMapItemListByKey(List.of("SEX"));
        assertEquals(1, res.size());
        assertTrue(res.containsKey("SEX"));

        // 2. removeItemByIds 空与非空
        assertFalse(manager.removeItemByIds(Collections.emptyList()));

        doReturn(Collections.emptyList()).when(manager).listByIds(List.of(99L));
        assertFalse(manager.removeItemByIds(List.of(99L)));

        doReturn(List.of(item)).when(manager).listByIds(List.of(1L));
        doReturn(true).when(manager).removeByIds(List.of(1L));
        assertTrue(manager.removeItemByIds(List.of(1L)));
        verify(cachePlusOps).del(any(com.dalio.basic.model.cache.CacheHashKey[].class));
    }
}
