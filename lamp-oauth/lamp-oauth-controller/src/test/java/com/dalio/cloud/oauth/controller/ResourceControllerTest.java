package com.dalio.cloud.oauth.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.oauth.biz.ResourceBiz;
import com.dalio.cloud.oauth.biz.StpInterfaceBiz;
import com.dalio.cloud.oauth.vo.result.VisibleResourceVO;
import com.dalio.cloud.system.enumeration.system.ClientTypeEnum;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ResourceController 单元测试
 */
class ResourceControllerTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试 visible 接口所有分支（显式 employeeId、SysUser 提取、ContextUtil 回退、有无 applicationId）")
    void testVisible() {
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        ignoreProperties.setAuthEnabled(true);
        ignoreProperties.setCaseSensitive(false);

        ResourceBiz oauthResourceBiz = Mockito.mock(ResourceBiz.class);
        BaseRoleService baseRoleService = Mockito.mock(BaseRoleService.class);
        StpInterfaceBiz stpInterfaceBiz = Mockito.mock(StpInterfaceBiz.class);

        ResourceController controller = new ResourceController(ignoreProperties, oauthResourceBiz, baseRoleService, stpInterfaceBiz);

        when(baseRoleService.findRoleCodeByEmployeeId(100L)).thenReturn(Collections.singletonList("ROLE_ADMIN"));
        when(oauthResourceBiz.findVisibleResource(100L, 1L)).thenReturn(Collections.singletonList("res:view"));
        when(oauthResourceBiz.findVisibleRouter(1L, 100L, "sub", ClientTypeEnum.LAMP_WEB)).thenReturn(Collections.emptyList());

        // 分支 1: 传入显式 employeeId 与 applicationId
        R<VisibleResourceVO> r1 = controller.visible(null, ClientTypeEnum.LAMP_WEB, 100L, 1L, "sub");
        assertNotNull(r1.getData());
        assertTrue(r1.getData().getEnabled());
        assertEquals("ROLE_ADMIN", r1.getData().getRoleList().get(0));
        assertEquals("res:view", r1.getData().getResourceList().get(0));

        // 分支 2: employeeId == null, 从 sysUser 提取, applicationId == null
        SysUser user = new SysUser();
        user.setEmployeeId(200L);
        when(baseRoleService.findRoleCodeByEmployeeId(200L)).thenReturn(Collections.singletonList("ROLE_USER"));
        when(oauthResourceBiz.findVisibleResource(200L, null)).thenReturn(Collections.emptyList());
        when(oauthResourceBiz.findAllVisibleRouter(200L, null, null)).thenReturn(Collections.emptyList());

        R<VisibleResourceVO> r2 = controller.visible(user, null, null, null, null);
        assertNotNull(r2.getData());
        assertEquals("ROLE_USER", r2.getData().getRoleList().get(0));

        // 分支 3: employeeId <= 0, sysUser == null, 从 ContextUtil 回退
        ContextUtil.setEmployeeId(300L);
        when(baseRoleService.findRoleCodeByEmployeeId(300L)).thenReturn(Collections.emptyList());
        when(oauthResourceBiz.findVisibleResource(300L, null)).thenReturn(Collections.emptyList());
        when(oauthResourceBiz.findAllVisibleRouter(300L, null, null)).thenReturn(Collections.emptyList());

        R<VisibleResourceVO> r3 = controller.visible(null, null, 0L, null, null);
        assertNotNull(r3.getData());
    }

    @Test
    @DisplayName("测试 visibleResource 接口")
    void testVisibleResource() {
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        ResourceBiz oauthResourceBiz = Mockito.mock(ResourceBiz.class);
        BaseRoleService baseRoleService = Mockito.mock(BaseRoleService.class);
        StpInterfaceBiz stpInterfaceBiz = Mockito.mock(StpInterfaceBiz.class);

        ResourceController controller = new ResourceController(ignoreProperties, oauthResourceBiz, baseRoleService, stpInterfaceBiz);

        when(oauthResourceBiz.findVisibleResource(100L, 2L)).thenReturn(Collections.singletonList("sys:user:view"));

        R<List<String>> result = controller.visibleResource(100L, 2L);
        assertTrue(result.getIsSuccess());
        assertEquals("sys:user:view", result.getData().get(0));
        verify(oauthResourceBiz).findVisibleResource(100L, 2L);
    }

    @Test
    @DisplayName("测试 checkEmployeeHaveApplication 接口")
    void testCheckEmployeeHaveApplication() {
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        ResourceBiz oauthResourceBiz = Mockito.mock(ResourceBiz.class);
        BaseRoleService baseRoleService = Mockito.mock(BaseRoleService.class);
        StpInterfaceBiz stpInterfaceBiz = Mockito.mock(StpInterfaceBiz.class);

        ResourceController controller = new ResourceController(ignoreProperties, oauthResourceBiz, baseRoleService, stpInterfaceBiz);

        ContextUtil.setEmployeeId(500L);
        when(oauthResourceBiz.checkEmployeeHaveApplication(500L, 10L)).thenReturn(true);

        R<Boolean> result = controller.checkEmployeeHaveApplication(10L);
        assertTrue(result.getIsSuccess());
        assertTrue(result.getData());
        verify(oauthResourceBiz).checkEmployeeHaveApplication(500L, 10L);
    }

    @Test
    @DisplayName("测试 getPermissionList 与 getRoleList 接口")
    void testStpInterfaceMethods() {
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        ResourceBiz oauthResourceBiz = Mockito.mock(ResourceBiz.class);
        BaseRoleService baseRoleService = Mockito.mock(BaseRoleService.class);
        StpInterfaceBiz stpInterfaceBiz = Mockito.mock(StpInterfaceBiz.class);

        ResourceController controller = new ResourceController(ignoreProperties, oauthResourceBiz, baseRoleService, stpInterfaceBiz);

        when(stpInterfaceBiz.getPermissionList()).thenReturn(Collections.singletonList("perm:all"));
        when(stpInterfaceBiz.getRoleList()).thenReturn(Collections.singletonList("admin"));

        R<List<String>> permR = controller.getPermissionList();
        assertTrue(permR.getIsSuccess());
        assertEquals("perm:all", permR.getData().get(0));

        R<List<String>> roleR = controller.getRoleList();
        assertTrue(roleR.getIsSuccess());
        assertEquals("admin", roleR.getData().get(0));
    }
}
