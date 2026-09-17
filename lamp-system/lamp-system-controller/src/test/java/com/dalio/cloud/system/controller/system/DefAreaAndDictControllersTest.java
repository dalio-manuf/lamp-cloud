package com.dalio.cloud.system.controller.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.base.R;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.database.mybatis.conditions.query.QueryWrap;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.entity.system.DefArea;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.entity.system.DefParameter;
import com.dalio.cloud.system.service.system.DefAreaService;
import com.dalio.cloud.system.service.system.DefClientService;
import com.dalio.cloud.system.service.system.DefDictItemService;
import com.dalio.cloud.system.service.system.DefDictService;
import com.dalio.cloud.system.service.system.DefLoginLogService;
import com.dalio.cloud.system.service.system.DefParameterService;
import com.dalio.cloud.system.vo.query.system.DefAreaPageQuery;
import com.dalio.cloud.system.vo.query.system.DefDictItemPageQuery;
import com.dalio.cloud.system.vo.query.system.DefDictPageQuery;
import com.dalio.cloud.system.vo.query.system.DefParameterPageQuery;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DefAreaController, DefClientController, DefDictController,
 * DefDictItemController, DefParameterController 与 DefLoginLogController 单元测试
 */
class DefAreaAndDictControllersTest {

    @Test
    @DisplayName("测试 DefAreaController 接口")
    void testDefAreaController() {
        DefAreaService service = mock(DefAreaService.class);
        EchoService echoService = mock(EchoService.class);
        DefAreaController controller = new DefAreaController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.findTree(any())).thenReturn(List.of(new DefArea()));
        R<List<DefArea>> treeR = controller.tree(new DefAreaPageQuery());
        assertTrue(treeR.getIsSuccess());
        assertEquals(1, treeR.getData().size());

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        controller.download(2, req, resp);
        verify(service).downloadJson(2, req, resp);

        when(service.findLazyList(1L)).thenReturn(List.of(new DefArea()));
        R<List<DefArea>> lazyR = controller.lazyList(1L);
        assertTrue(lazyR.getIsSuccess());
        assertEquals(1, lazyR.getData().size());

        when(service.check("area_code", 1L)).thenReturn(true);
        R<Boolean> checkR = controller.check("area_code", 1L);
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());
    }

    @Test
    @DisplayName("测试 DefClientController 接口")
    void testDefClientController() {
        EchoService echoService = mock(EchoService.class);
        DefClientService service = mock(DefClientService.class);
        DefClientController controller = new DefClientController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());
    }

    @Test
    @DisplayName("测试 DefDictController 接口与条件构造")
    void testDefDictController() {
        DefDictService service = mock(DefDictService.class);
        EchoService echoService = mock(EchoService.class);
        DefDictController controller = new DefDictController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        DefDict model = new DefDict();
        model.setKey("dictKey");
        model.setName("dictName");
        DefDictPageQuery query = new DefDictPageQuery();
        query.setClassify(List.of("10"));
        query.setState(List.of(true));
        PageParams<DefDictPageQuery> params = new PageParams<>();
        params.setCurrent(1);
        params.setSize(10);
        params.setModel(query);
        QueryWrap<DefDict> wrap = controller.handlerWrapper(model, params);
        assertNotNull(wrap);

        when(service.deleteDict(List.of(1L, 2L))).thenReturn(true);
        R<Boolean> delR = controller.handlerDelete(List.of(1L, 2L));
        assertTrue(delR.getIsSuccess());
        assertTrue(delR.getData());

        when(service.checkByKey("k", 1L)).thenReturn(true);
        R<Boolean> checkR = controller.check("k", 1L);
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());

        when(service.importDictByEnum(anyList())).thenReturn(true);
        R<Boolean> importR = controller.importDictByEnum(List.of(new DefDictResultVO()));
        assertTrue(importR.getIsSuccess());
        assertTrue(importR.getData());
    }

    @Test
    @DisplayName("测试 DefDictItemController 接口、校验与条件构造")
    void testDefDictItemController() {
        DefDictItemService service = mock(DefDictItemService.class);
        EchoService echoService = mock(EchoService.class);
        DefDictItemController controller = new DefDictItemController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        DefDictItemPageQuery query = new DefDictItemPageQuery();
        query.setParentId(1L);
        PageParams<DefDictItemPageQuery> params = new PageParams<>();
        params.setCurrent(1);
        params.setSize(10);
        params.setModel(query);
        assertDoesNotThrow(() -> controller.handlerQueryParams(params));

        query.setParentId(null);
        assertThrows(Exception.class, () -> controller.handlerQueryParams(params));

        DefDict model = new DefDict();
        model.setParentId(1L);
        model.setKey("itemKey");
        model.setName("itemName");
        query.setParentId(1L);
        query.setClassify(List.of("10"));
        query.setState(List.of(true));
        QueryWrap<DefDict> wrap = controller.handlerWrapper(model, params);
        assertNotNull(wrap);

        when(service.checkItemByKey("k", 1L, 2L)).thenReturn(true);
        R<Boolean> checkR = controller.check("k", 1L, 2L);
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());
    }

    @Test
    @DisplayName("测试 DefParameterController 接口与条件构造")
    void testDefParameterController() {
        DefParameterService service = mock(DefParameterService.class);
        EchoService echoService = mock(EchoService.class);
        DefParameterController controller = new DefParameterController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        DefParameter model = new DefParameter();
        model.setKey("paramKey");
        model.setName("paramName");
        model.setValue("paramVal");
        model.setRemarks("remarks");
        DefParameterPageQuery query = new DefParameterPageQuery();
        query.setState(List.of(true));
        PageParams<DefParameterPageQuery> params = new PageParams<>();
        params.setCurrent(1);
        params.setSize(10);
        params.setModel(query);
        QueryWrap<DefParameter> wrap = controller.handlerWrapper(model, params);
        assertNotNull(wrap);

        when(service.checkKey("pk", 1L)).thenReturn(true);
        R<Boolean> checkR = controller.check("pk", 1L);
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());
    }

    @Test
    @DisplayName("测试 DefLoginLogController 各清理类型与分支")
    void testDefLoginLogController() {
        DefLoginLogService service = mock(DefLoginLogService.class);
        EchoService echoService = mock(EchoService.class);
        DefLoginLogController controller = new DefLoginLogController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.clearLog(any(), any())).thenReturn(true);

        for (int i = 1; i <= 8; i++) {
            R<Boolean> r = controller.clear(i);
            assertTrue(r.getIsSuccess());
            assertTrue(r.getData());
        }

        R<Boolean> nullR = controller.clear(null);
        assertTrue(nullR.getIsSuccess());

        R<Boolean> failR = controller.clear(99);
        assertFalse(failR.getIsSuccess());
    }
}
