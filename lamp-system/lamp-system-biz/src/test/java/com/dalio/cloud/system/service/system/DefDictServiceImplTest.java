package com.dalio.cloud.system.service.system;

import com.baidu.fsg.uid.UidGenerator;
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
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.service.system.impl.DefDictServiceImpl;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;
import com.dalio.cloud.system.vo.save.system.DefDictItemSaveVO;
import com.dalio.cloud.system.vo.save.system.DefDictSaveVO;
import com.dalio.cloud.system.vo.update.system.DefDictItemUpdateVO;
import com.dalio.cloud.system.vo.update.system.DefDictUpdateVO;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefDictServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefDict.class);
    }

    @Test
    @DisplayName("测试 checkByKey 校验")
    void testCheckByKey() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        assertThrows(ArgumentException.class, () -> service.checkByKey("", 1L));

        when(dictManager.count(any())).thenReturn(1L);
        assertTrue(service.checkByKey("SEX", 1L));

        when(dictManager.count(any())).thenReturn(0L);
        assertFalse(service.checkByKey("SEX", 1L));
    }

    @Test
    @DisplayName("测试 save 字典与条目保存")
    void testSave() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        DefDictSaveVO saveVO = new DefDictSaveVO();
        saveVO.setKey("GENDER");
        saveVO.setName("性别");

        // 1. 重复 key
        when(dictManager.count(any())).thenReturn(1L);
        assertThrows(ArgumentException.class, () -> service.save(saveVO));

        // 2. 正常保存且包含条目
        when(dictManager.count(any())).thenReturn(0L);
        when(dictManager.save(any())).thenReturn(true);

        DefDictItemSaveVO itemVO = new DefDictItemSaveVO();
        itemVO.setKey("1");
        itemVO.setName("男");
        saveVO.setInsertList(List.of(itemVO));

        DefDict dict = service.save(saveVO);
        assertNotNull(dict);
        verify(dictManager).saveBatch(anyList());
        verify(cachePlusOps, atLeastOnce()).hSet(any(), any());
    }

    @Test
    @DisplayName("测试 deleteDict 删除字典及清理缓存")
    void testDeleteDict() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        assertFalse(service.deleteDict(Collections.emptyList()));

        when(dictManager.listByIds(anyList())).thenReturn(Collections.emptyList());
        assertFalse(service.deleteDict(List.of(1L)));

        DefDict dict = new DefDict();
        dict.setId(1L);
        dict.setKey("SEX");
        when(dictManager.listByIds(anyList())).thenReturn(List.of(dict));
        when(dictManager.removeByIds(anyList())).thenReturn(true);

        assertTrue(service.deleteDict(List.of(1L)));
        verify(dictManager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        verify(cachePlusOps).del(any(com.dalio.basic.model.cache.CacheHashKey[].class));
    }

    @Test
    @DisplayName("测试 updateById 字典更新与缓存同步")
    void testUpdateById() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        DefDictUpdateVO updateVO = new DefDictUpdateVO();
        updateVO.setId(10L);
        updateVO.setKey("SEX_NEW");
        updateVO.setName("性别");

        // 重复 key
        when(dictManager.count(any())).thenReturn(1L);
        assertThrows(ArgumentException.class, () -> service.updateById(updateVO));

        // 正常更新
        when(dictManager.count(any())).thenReturn(0L);
        DefDict oldDict = new DefDict();
        oldDict.setId(10L);
        oldDict.setKey("SEX_OLD");
        when(dictManager.getById(10L)).thenReturn(oldDict);

        DefDictItemUpdateVO itemUpdate = new DefDictItemUpdateVO();
        itemUpdate.setKey("1");
        itemUpdate.setName("男");
        updateVO.setUpdateList(List.of(itemUpdate));
        updateVO.setDeleteList(List.of(101L));

        DefDict updated = service.updateById(updateVO);
        assertNotNull(updated);
        verify(dictManager).updateById(any(DefDict.class));
        verify(dictManager).update(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        verify(dictManager).removeItemByIds(List.of(101L));
        verify(dictManager).updateBatchById(anyList());
        verify(cachePlusOps, atLeast(2)).del(any(com.dalio.basic.model.cache.CacheKey.class));
    }

    @Test
    @DisplayName("测试 copy 复制字典与条目")
    void testCopy() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        when(dictManager.getById(99L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.copy(99L));

        DefDict oldDict = new DefDict();
        oldDict.setId(10L);
        oldDict.setKey("ORIGIN");
        oldDict.setName("原始");
        when(dictManager.getById(10L)).thenReturn(oldDict);

        DefDict item = new DefDict();
        item.setId(1001L);
        item.setKey("ITEM1");
        when(dictManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(item));

        DefDict copied = service.copy(10L);
        assertNotNull(copied);
        assertEquals("ORIGIN_copy", copied.getKey());
        verify(dictManager).saveBatch(anyList());
    }

    @Test
    @DisplayName("测试 importDictByEnum 枚举导入")
    void testImportDictByEnum() {
        DefDictManager dictManager = mock(DefDictManager.class);
        CachePlusOps cachePlusOps = mock(CachePlusOps.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(10001L, 10002L, 10003L);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", dictManager);

        assertTrue(service.importDictByEnum(Collections.emptyList()));

        // 1. 已存在字典更新
        DefDictResultVO vo1 = new DefDictResultVO();
        vo1.setKey("ENUM_DICT_1");
        vo1.setName("枚举1");
        DefDictItemResultVO itemVo1 = new DefDictItemResultVO();
        itemVo1.setKey("VAL1");
        itemVo1.setName("值1");
        vo1.setItemList(List.of(itemVo1));

        // 2. 不存在字典新增
        DefDictResultVO vo2 = new DefDictResultVO();
        vo2.setKey("ENUM_DICT_2");
        vo2.setName("枚举2");
        DefDictItemResultVO itemVo2 = new DefDictItemResultVO();
        itemVo2.setKey("VAL2");
        itemVo2.setName("值2");
        vo2.setItemList(List.of(itemVo2));

        DefDict existingDict = new DefDict();
        existingDict.setId(101L);
        existingDict.setKey("ENUM_DICT_1");
        existingDict.setName("旧名");

        when(dictManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class)))
                .thenReturn(List.of(existingDict)) // super.list(Wraps...)
                .thenReturn(Collections.emptyList()); // superManager.list(Wraps...) 字典项

        Boolean res = service.importDictByEnum(List.of(vo1, vo2));
        assertTrue(res);
        verify(dictManager, atLeastOnce()).saveBatch(anyList());
        verify(dictManager, atLeastOnce()).updateBatchById(anyList());
        verify(cachePlusOps).del(anyList());

        // findItemByDictId
        when(dictManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(new DefDict()));
        assertEquals(1, service.findItemByDictId(101L).size());
    }
}
