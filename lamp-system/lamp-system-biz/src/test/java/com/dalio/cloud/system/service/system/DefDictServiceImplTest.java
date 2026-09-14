package com.dalio.cloud.system.service.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.cache.repository.CachePlusOps;
import com.dalio.cloud.common.constant.DefValConstants;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.service.system.impl.DefDictServiceImpl;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 字典服务实现类单元测试 (验证枚举导入去重修复与层级删除)
 */
class DefDictServiceImplTest {

    @Test
    @DisplayName("测试 deleteDict 级联删除字典与条目并淘汰缓存")
    void testDeleteDict() {
        DefDictManager manager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);
        com.baidu.fsg.uid.UidGenerator uidGenerator = Mockito.mock(com.baidu.fsg.uid.UidGenerator.class);

        DefDictServiceImpl service = Mockito.spy(new DefDictServiceImpl(cachePlusOps, uidGenerator));
        ReflectionTestUtils.setField(service, "superManager", manager);

        // 1. 空参数
        assertFalse(service.deleteDict(Collections.emptyList()));

        // 2. 正常级联删除
        DefDict dict = new DefDict();
        dict.setId(10L);
        dict.setKey("SEX");
        when(manager.listByIds(List.of(10L))).thenReturn(List.of(dict));
        doReturn(true).when(service).removeByIds(List.of(10L));

        boolean result = service.deleteDict(List.of(10L));
        assertTrue(result);
        verify(manager).remove(any());
        verify(cachePlusOps).del(any(com.dalio.basic.model.cache.CacheHashKey[].class));
    }

    @Test
    @DisplayName("测试 importDictByEnum 枚举字典导入与父级隔离防御修复")
    void testImportDictByEnum() {
        DefDictManager manager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);
        com.baidu.fsg.uid.UidGenerator uidGenerator = Mockito.mock(com.baidu.fsg.uid.UidGenerator.class);

        DefDictServiceImpl service = Mockito.spy(new DefDictServiceImpl(cachePlusOps, uidGenerator));
        ReflectionTestUtils.setField(service, "superManager", manager);

        // 1. 空参数直接返回 true
        assertTrue(service.importDictByEnum(Collections.emptyList()));

        // 2. 导入字典数据：包含已有字典更新与新字典插入
        DefDict existingDict = new DefDict();
        existingDict.setId(100L);
        existingDict.setKey("GENDER");
        existingDict.setParentId(DefValConstants.PARENT_ID);
        existingDict.setName("旧性别");

        // mock 查询库中已有父级字典
        doReturn(List.of(existingDict)).when(service).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        // mock 库中已有字典项
        DefDict existingItem = new DefDict();
        existingItem.setId(201L);
        existingItem.setParentId(100L);
        existingItem.setKey("MALE");
        existingItem.setName("男");
        when(manager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(existingItem));

        List<DefDictResultVO> inputList = new ArrayList<>();
        // VO 1: 更新 GENDER
        DefDictResultVO vo1 = new DefDictResultVO();
        vo1.setKey("GENDER");
        vo1.setName("新性别");
        DefDictItemResultVO item1 = new DefDictItemResultVO();
        item1.setKey("MALE");
        item1.setName("男性");
        DefDictItemResultVO item2 = new DefDictItemResultVO();
        item2.setKey("FEMALE");
        item2.setName("女性");
        vo1.setItemList(List.of(item1, item2));
        inputList.add(vo1);

        // VO 2: 新增 STATUS
        DefDictResultVO vo2 = new DefDictResultVO();
        vo2.setKey("STATUS");
        vo2.setName("系统状态");
        inputList.add(vo2);

        Boolean importResult = service.importDictByEnum(inputList);
        assertTrue(importResult);

        // 验证批量新增与更新被调用
        verify(manager, atLeastOnce()).updateBatchById(anyList());
        verify(manager, atLeastOnce()).saveBatch(anyList());
        verify(cachePlusOps, atLeastOnce()).del(anyList());
    }

    @Test
    @DisplayName("测试 checkByKey 字典编码重复检查")
    void testCheckByKey() {
        DefDictManager manager = Mockito.mock(DefDictManager.class);
        CachePlusOps cachePlusOps = Mockito.mock(CachePlusOps.class);
        com.baidu.fsg.uid.UidGenerator uidGenerator = Mockito.mock(com.baidu.fsg.uid.UidGenerator.class);

        DefDictServiceImpl service = new DefDictServiceImpl(cachePlusOps, uidGenerator);
        ReflectionTestUtils.setField(service, "superManager", manager);

        when(manager.count(any())).thenReturn(1L);
        assertTrue(service.checkByKey("STATUS", 1L));

        when(manager.count(any())).thenReturn(0L);
        assertFalse(service.checkByKey("STATUS", 1L));
    }
}
