package com.dalio.cloud.system.controller.application;

import com.dalio.basic.base.R;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.system.biz.application.DefResourceBiz;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.service.application.DefApplicationService;
import com.dalio.cloud.system.service.application.DefResourceService;
import com.dalio.cloud.system.vo.result.application.ApplicationResourceResultVO;
import com.dalio.cloud.system.vo.result.application.DefResourceResultVO;
import com.dalio.cloud.system.vo.save.application.DefResourceSaveVO;
import com.dalio.cloud.system.vo.update.application.DefResourceUpdateVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DefApplicationController 与 DefResourceController 单元测试
 */
class DefApplicationAndResourceControllerTest {

    @Test
    @DisplayName("测试 DefApplicationController 各项接口与校验")
    void testDefApplicationController() {
        DefApplicationService service = mock(DefApplicationService.class);
        EchoService echoService = mock(EchoService.class);

        DefApplicationController controller = new DefApplicationController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.check(1L, "app")).thenReturn(true);
        R<Boolean> checkR = controller.check(1L, "app");
        assertTrue(checkR.getIsSuccess());
        assertTrue(checkR.getData());

        when(service.findApplicationResourceList()).thenReturn(List.of(new ApplicationResourceResultVO()));
        R<List<ApplicationResourceResultVO>> resR1 = controller.findApplicationResourceList();
        assertTrue(resR1.getIsSuccess());
        assertEquals(1, resR1.getData().size());

        when(service.findAvailableApplicationResourceList()).thenReturn(List.of(new ApplicationResourceResultVO()));
        R<List<ApplicationResourceResultVO>> resR2 = controller.findAvailableApplicationResourceList();
        assertTrue(resR2.getIsSuccess());
        assertEquals(1, resR2.getData().size());

        when(service.findAvailableApplicationDataScopeList()).thenReturn(List.of(new ApplicationResourceResultVO()));
        R<List<ApplicationResourceResultVO>> resR3 = controller.findAvailableApplicationDataScopeList();
        assertTrue(resR3.getIsSuccess());
        assertEquals(1, resR3.getData().size());
    }

    @Test
    @DisplayName("测试 DefResourceController 增删改查与树结构及移动")
    void testDefResourceController() {
        DefResourceService service = mock(DefResourceService.class);
        EchoService echoService = mock(EchoService.class);
        DefResourceBiz biz = mock(DefResourceBiz.class);

        DefResourceController controller = new DefResourceController(echoService, biz);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        // check, checkPath, checkName
        when(service.check(1L, "code")).thenReturn(true);
        assertTrue(controller.check(1L, "code").getData());

        when(service.checkPath(1L, 2L, "/p")).thenReturn(true);
        assertTrue(controller.checkPath(1L, 2L, "/p").getData());

        when(service.checkName(1L, 2L, "n")).thenReturn(true);
        assertTrue(controller.checkName(1L, 2L, "n").getData());

        // handlerSave, handlerDelete, handlerUpdate
        DefResource res = new DefResource();
        res.setId(100L);
        when(service.saveWithCache(any())).thenReturn(res);
        assertEquals(res, controller.handlerSave(new DefResourceSaveVO()).getData());

        when(biz.removeByIdWithCache(anyList())).thenReturn(true);
        assertTrue(controller.handlerDelete(List.of(100L)).getData());

        when(service.updateWithCacheById(any())).thenReturn(res);
        assertEquals(res, controller.handlerUpdate(new DefResourceUpdateVO()).getData());

        // allTree
        DefResource qRes = new DefResource();
        qRes.setId(100L);
        qRes.setParentId(0L);
        when(service.list(any())).thenReturn(List.of(qRes));
        R<List<DefResourceResultVO>> treeR = controller.allTree(new DefResource());
        assertTrue(treeR.getIsSuccess());
        assertEquals(1, treeR.getData().size());

        // moveResource
        R<Boolean> moveR = controller.moveResource(100L, 0L);
        assertTrue(moveR.getIsSuccess());
        verify(service).moveResource(100L, 0L);

        // get
        DefResourceResultVO vo = new DefResourceResultVO();
        vo.setId(100L);
        when(service.getResourceById(100L)).thenReturn(vo);
        assertEquals(vo, controller.get(100L).getData());
    }
}
