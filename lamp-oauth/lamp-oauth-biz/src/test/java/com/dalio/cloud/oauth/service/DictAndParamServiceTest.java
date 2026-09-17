package com.dalio.cloud.oauth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.oauth.service.impl.DictServiceImpl;
import com.dalio.cloud.oauth.service.impl.ParamServiceImpl;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.manager.system.DefParameterManager;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 字典服务与参数服务单元测试
 */
class DictAndParamServiceTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试 DictServiceImpl findByIds i18n 国际化转换与回退")
    void testDictServiceImplI18n() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        SystemProperties properties = new SystemProperties();
        DictServiceImpl dictService = new DictServiceImpl(dictManager, properties);

        // 1. 空参数
        Map<Serializable, Object> emptyRes = dictService.findByIds(Collections.emptySet());
        assertTrue(emptyRes.isEmpty());

        // 2. 正常查询并带 i18n
        DefDict dict1 = new DefDict();
        dict1.setId(1L);
        dict1.setName("男");
        dict1.setI18nJson("{\"zh_CN\":\"男\",\"en_US\":\"Male\"}");

        DefDict dict2 = new DefDict();
        dict2.setId(2L);
        dict2.setName("女");
        dict2.setI18nJson("invalid_json"); // 非法 JSON 回退到默认名称

        Map<Serializable, DefDict> mockDefMap = new HashMap<>();
        mockDefMap.put(1L, dict1);
        mockDefMap.put(2L, dict2);

        Set<Serializable> queryKeys = Set.of(1L, 2L);
        when(dictManager.findByIds(queryKeys)).thenReturn(mockDefMap);

        // 设置 en_US 语言环境
        ContextUtil.setLocale("en_US");
        Map<Serializable, Object> enResult = dictService.findByIds(queryKeys);
        assertEquals("Male", enResult.get(1L));
        assertEquals("女", enResult.get(2L));

        // 设置 zh_CN 语言环境
        ContextUtil.setLocale("zh_CN");
        Map<Serializable, Object> zhResult = dictService.findByIds(queryKeys);
        assertEquals("男", zhResult.get(1L));

        // 3. syncEnumToDict 委托
        dictService.syncEnumToDict();
        verify(dictManager).syncEnumToDict(any());

        // 4. findDictItemByType
        List<String> typeKeys = List.of("SEX");
        Map<String, List<DefDictItemResultVO>> mockItemMap = Map.of("SEX", List.of(new DefDictItemResultVO()));
        when(dictManager.findDictMapItemListByKey(typeKeys)).thenReturn(mockItemMap);
        assertEquals(mockItemMap, dictService.findDictItemByType(typeKeys));
        assertTrue(dictService.findDictItemByType(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("测试 DictServiceImpl init 与 findAll")
    void testDictServiceImplFindAllAndInit() {
        DefDictManager dictManager = Mockito.mock(DefDictManager.class);
        SystemProperties properties = new SystemProperties();
        DictServiceImpl dictService = new DictServiceImpl(dictManager, properties);

        // 1. 空 package 预警
        properties.setEnumPackage("");
        dictService.init();

        // 2. 正常初始化扫描
        properties.setEnumPackage("com.dalio.cloud.model.enumeration");
        dictService.init();

        // 3. findAll - 空数据库列表
        when(dictManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Collections.emptyList());
        List<com.dalio.cloud.system.vo.result.system.DefDictResultVO> list = dictService.findAll();
        assertNotNull(list);

        // 4. findAll - 模拟数据库中已有字典和字典项数据
        DefDict existingDict = new DefDict();
        existingDict.setId(101L);
        existingDict.setKey("BooleanEnum");
        existingDict.setName("是否");

        DefDict existingItem = new DefDict();
        existingItem.setId(201L);
        existingItem.setParentId(101L);
        existingItem.setKey("true");
        existingItem.setName("是");

        when(dictManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class)))
                .thenReturn(List.of(existingDict))
                .thenReturn(List.of(existingItem));

        List<com.dalio.cloud.system.vo.result.system.DefDictResultVO> populatedList = dictService.findAll();
        assertNotNull(populatedList);
        assertFalse(populatedList.isEmpty());
    }

    @Test
    @DisplayName("测试 ParamServiceImpl 查询与兜底")
    void testParamServiceImpl() {
        DefParameterManager paramManager = Mockito.mock(DefParameterManager.class);
        ParamServiceImpl paramService = new ParamServiceImpl(paramManager);

        // 1. 空参数
        assertTrue(paramService.findParamMapByKey(Collections.emptyList()).isEmpty());
        assertTrue(paramService.findByIds(Collections.emptySet()).isEmpty());

        // 2. 正常查询
        List<String> keys = List.of("SYS_NAME");
        Map<String, String> resultMap = Map.of("SYS_NAME", "LampCloud");
        when(paramManager.findParamMapByKey(keys)).thenReturn(resultMap);
        assertEquals(resultMap, paramService.findParamMapByKey(keys));

        Set<Serializable> idKeys = Set.of(100L);
        Map<Serializable, Object> idResultMap = Map.of(100L, "LampCloud");
        when(paramManager.findByIds(idKeys)).thenReturn(idResultMap);
        assertEquals(idResultMap, paramService.findByIds(idKeys));
    }
}
