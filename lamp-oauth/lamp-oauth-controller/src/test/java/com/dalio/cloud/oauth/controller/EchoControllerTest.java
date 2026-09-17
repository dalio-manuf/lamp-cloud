package com.dalio.cloud.oauth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.service.user.BasePositionService;
import com.dalio.cloud.oauth.service.DictService;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EchoController 单元测试
 */
class EchoControllerTest {

    @Test
    @DisplayName("测试 EchoController 数据注入查询接口")
    void testEchoController() {
        DictService dictService = Mockito.mock(DictService.class);
        BaseOrgService baseOrgService = Mockito.mock(BaseOrgService.class);
        BasePositionService basePositionService = Mockito.mock(BasePositionService.class);

        EchoController controller = new EchoController(dictService, baseOrgService, basePositionService);

        Set<Serializable> ids = Collections.singleton(1L);

        // 测试 findStationByIds
        Map<Serializable, Object> stationMap = Collections.singletonMap(1L, "经理");
        when(basePositionService.findByIds(ids)).thenReturn(stationMap);
        Map<Serializable, Object> stationResult = controller.findStationByIds(ids);
        assertNotNull(stationResult);
        assertEquals("经理", stationResult.get(1L));
        verify(basePositionService).findByIds(ids);

        // 测试 findOrgByIds
        Map<Serializable, Object> orgMap = Collections.singletonMap(1L, "总公司");
        when(baseOrgService.findByIds(ids)).thenReturn(orgMap);
        Map<Serializable, Object> orgResult = controller.findOrgByIds(ids);
        assertNotNull(orgResult);
        assertEquals("总公司", orgResult.get(1L));
        verify(baseOrgService).findByIds(ids);

        // 测试 findDictByIds
        Set<Serializable> dictIds = Collections.singleton("SEX_MAN");
        Map<Serializable, Object> dictMap = Collections.singletonMap("SEX_MAN", "男");
        when(dictService.findByIds(dictIds)).thenReturn(dictMap);
        Map<Serializable, Object> dictResult = controller.findDictByIds(dictIds);
        assertNotNull(dictResult);
        assertEquals("男", dictResult.get("SEX_MAN"));
        verify(dictService).findByIds(dictIds);
    }
}
