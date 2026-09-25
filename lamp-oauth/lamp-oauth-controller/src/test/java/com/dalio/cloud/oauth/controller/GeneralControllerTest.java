package com.dalio.cloud.oauth.controller;

import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.service.DictService;
import com.dalio.cloud.oauth.service.ParamService;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GeneralController 单元测试
 */
class GeneralControllerTest {

    @Test
    @DisplayName("测试 GeneralController 通用字典与参数接口")
    void testGeneralController() {
        DictService dictService = Mockito.mock(DictService.class);
        ParamService paramService = Mockito.mock(ParamService.class);

        GeneralController controller = new GeneralController(dictService, paramService);

        // syncEnumToDict
        R<Boolean> syncR = controller.syncEnumToDict();
        assertTrue(syncR.getIsSuccess());
        assertTrue(syncR.getData());
        verify(dictService).syncEnumToDict();

        // findDictItemByType
        List<String> types = Arrays.asList("SEX", "NATION");
        Map<String, List<DefDictItemResultVO>> dictMap = Collections.singletonMap("SEX", Collections.emptyList());
        when(dictService.findDictItemByType(types)).thenReturn(dictMap);
        R<Map<String, List<DefDictItemResultVO>>> dictItemR = controller.findDictItemByType(types);
        assertTrue(dictItemR.getIsSuccess());
        assertEquals(dictMap, dictItemR.getData());
        verify(dictService).findDictItemByType(types);

        // findParams
        List<String> keys = Collections.singletonList("SYS_NAME");
        Map<String, String> paramMap = Collections.singletonMap("SYS_NAME", "LampCloud");
        when(paramService.findParamMapByKey(keys)).thenReturn(paramMap);
        R<Map<String, String>> paramR = controller.findParams(keys);
        assertTrue(paramR.getIsSuccess());
        assertEquals("LampCloud", paramR.getData().get("SYS_NAME"));
        verify(paramService).findParamMapByKey(keys);

        // findAll
        List<DefDictResultVO> enums = Collections.singletonList(new DefDictResultVO());
        when(dictService.findAll()).thenReturn(enums);
        R<List<DefDictResultVO>> enumR = controller.findAll();
        assertTrue(enumR.getIsSuccess());
        assertEquals(1, enumR.getData().size());
        verify(dictService).findAll();
    }
}
