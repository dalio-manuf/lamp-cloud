package com.dalio.cloud.msg.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.base.R;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.msg.service.DefInterfacePropertyService;
import com.dalio.cloud.msg.service.DefInterfaceService;
import com.dalio.cloud.msg.service.DefMsgTemplateService;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertyBatchSaveVO;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * DefInterfaceController, DefInterfacePropertyController 与 DefMsgTemplateController 单元测试
 */
class DefMsgControllersTest {

    @Test
    @DisplayName("测试 DefInterfaceController 接口与编码检测")
    void testDefInterfaceController() {
        DefInterfaceService service = mock(DefInterfaceService.class);
        EchoService echoService = mock(EchoService.class);
        DefInterfaceController controller = new DefInterfaceController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.check("if_code", 1L)).thenReturn(true);
        R<Boolean> checkR = controller.check(1L, "if_code");
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());
    }

    @Test
    @DisplayName("测试 DefInterfacePropertyController 批量保存与接口")
    void testDefInterfacePropertyController() {
        DefInterfacePropertyService service = mock(DefInterfacePropertyService.class);
        EchoService echoService = mock(EchoService.class);
        DefInterfacePropertyController controller = new DefInterfacePropertyController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.batchSave(any())).thenReturn(true);
        R<Boolean> batchR = controller.batchSave(new DefInterfacePropertyBatchSaveVO());
        assertTrue(batchR.getIsSuccess());
        assertTrue(batchR.getData());
    }

    @Test
    @DisplayName("测试 DefMsgTemplateController 接口与模板编码检测")
    void testDefMsgTemplateController() {
        DefMsgTemplateService service = mock(DefMsgTemplateService.class);
        EchoService echoService = mock(EchoService.class);
        DefMsgTemplateController controller = new DefMsgTemplateController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.check("tpl_code", 2L)).thenReturn(true);
        R<Boolean> checkR = controller.check(2L, "tpl_code");
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());
    }
}
