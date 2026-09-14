package com.dalio.cloud.oauth.biz;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.vo.result.user.VueRouter;
import com.dalio.cloud.common.constant.RoleConstant;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.model.vo.result.AppendixResultVO;
import com.dalio.cloud.oauth.service.impl.UserInfoServiceImpl;
import com.dalio.cloud.oauth.vo.param.RegisterByEmailVO;
import com.dalio.cloud.oauth.vo.param.RegisterByMobileVO;
import com.dalio.cloud.oauth.vo.result.DefUserInfoResultVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;
import com.dalio.cloud.model.enumeration.system.ResourceTypeEnum;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.enumeration.system.ClientTypeEnum;
import com.dalio.cloud.system.enumeration.tenant.ResourceOpenWithEnum;
import com.dalio.cloud.system.service.application.DefApplicationService;
import com.dalio.cloud.system.service.application.DefResourceService;
import com.dalio.cloud.system.service.tenant.DefUserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户业务、资源业务与权限门面单元测试
 */
class UserInfoAndBizTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cn.dev33.satoken.SaManager.setConfig(new cn.dev33.satoken.config.SaTokenConfig());
        cn.dev33.satoken.context.mock.SaTokenContextMockUtil.setMockContext();
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(req));
    }

    @AfterEach
    void tearDown() {
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        cn.dev33.satoken.context.mock.SaTokenContextMockUtil.clearContext();
        ContextUtil.remove();
        try {
            StpUtil.logout();
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("测试 UserInfoServiceImpl 用户信息查询与注册")
    void testUserInfoServiceImpl() {
        BaseEmployeeService employeeService = Mockito.mock(BaseEmployeeService.class);
        BaseOrgService orgService = Mockito.mock(BaseOrgService.class);
        DefUserService userService = Mockito.mock(DefUserService.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.setVerifyCaptcha(true);

        UserInfoServiceImpl service = new UserInfoServiceImpl(employeeService, orgService, userService, cacheOps, systemProperties);

        // 1. 用户不属于企业
        ContextUtil.setUserId(101L);
        when(employeeService.getEmployeeByUser(101L)).thenReturn(null);
        assertThrows(Exception.class, service::findCompanyAndDept);

        // 2. 正常获取企业与部门
        BaseEmployee employee = new BaseEmployee();
        employee.setId(201L);
        employee.setLastCompanyId(301L);
        employee.setLastDeptId(401L);
        when(employeeService.getEmployeeByUser(101L)).thenReturn(employee);

        BaseOrg org = new BaseOrg();
        org.setId(301L);
        when(orgService.findOrgByEmployeeId(201L)).thenReturn(List.of(org));

        OrgResultVO orgRes = service.findCompanyAndDept();
        assertNotNull(orgRes);
        assertEquals(201L, orgRes.getEmployeeId());
        assertEquals(301L, orgRes.getCurrentCompanyId());
        assertEquals(401L, orgRes.getCurrentDeptId());

        // 3. findDeptByCompany
        when(orgService.findDeptByEmployeeId(201L, 301L)).thenReturn(List.of(org));
        assertEquals(1, service.findDeptByCompany(301L, 201L).size());

        // 4. registerByMobile 密码不一致防御
        RegisterByMobileVO regMobile = new RegisterByMobileVO();
        regMobile.setMobile("13800001111");
        regMobile.setPassword("pass1");
        regMobile.setConfirmPassword("pass2");
        regMobile.setCode("1234");
        regMobile.setKey("key-1");

        @SuppressWarnings("unchecked")
        CacheResult<String> mockCode = Mockito.mock(CacheResult.class);
        when(mockCode.getValue()).thenReturn("1234");
        doReturn(mockCode).when(cacheOps).get(any(CacheKey.class));

        assertThrows(Exception.class, () -> service.registerByMobile(regMobile));

        // 手机注册成功，验证码缓存被删除
        regMobile.setConfirmPassword("pass1");
        String registeredMobile = service.registerByMobile(regMobile);
        assertEquals("13800001111", registeredMobile);
        verify(userService).register(any(DefUser.class));
        verify(cacheOps).del(any(CacheKey.class));

        // 5. registerByEmail 邮箱注册
        RegisterByEmailVO regEmail = new RegisterByEmailVO();
        regEmail.setEmail("user@example.com");
        regEmail.setPassword("pass1");
        regEmail.setConfirmPassword("pass1");
        regEmail.setCode("1234");
        regEmail.setKey("key-1");

        String registeredEmail = service.registerByEmail(regEmail);
        assertEquals("user@example.com", registeredEmail);
        verify(userService).registerByEmail(any(DefUser.class));

        // 6. registerTempAdmin
        Map<String, Object> tempAdmin = service.registerTempAdmin("DEMO");
        assertNotNull(tempAdmin);
        assertNotNull(tempAdmin.get("username"));
        assertNotNull(tempAdmin.get("password"));
    }

    @Test
    @DisplayName("测试 ResourceBiz 资源权限与路由树生成")
    void testResourceBiz() {
        DefResourceService resourceService = Mockito.mock(DefResourceService.class);
        DefApplicationService appService = Mockito.mock(DefApplicationService.class);
        BaseRoleService roleService = Mockito.mock(BaseRoleService.class);

        ResourceBiz resourceBiz = new ResourceBiz(resourceService, appService, roleService);

        // 1. 管理员可见资源
        when(roleService.checkRole(1001L, RoleConstant.TENANT_ADMIN)).thenReturn(true);
        DefResource res1 = new DefResource();
        res1.setId(10L);
        res1.setCode("user:view;user:add");
        when(resourceService.findResourceListByApplicationId(eq(List.of(1L)), any())).thenReturn(List.of(res1));

        List<String> visibleResources = resourceBiz.findVisibleResource(1001L, 1L);
        assertTrue(visibleResources.contains("user:view"));
        assertTrue(visibleResources.contains("user:add"));

        // 2. 非管理员无资源
        when(roleService.checkRole(2001L, RoleConstant.TENANT_ADMIN)).thenReturn(false);
        when(roleService.findResourceIdByEmployeeId(1L, 2001L)).thenReturn(Collections.emptyList());
        assertTrue(resourceBiz.findVisibleResource(2001L, 1L).isEmpty());

        // 3. 路由树解析 (支持 Vben5 / Soybean / 普通)
        DefResource menu = new DefResource();
        menu.setId(1L);
        menu.setParentId(0L);
        menu.setName("System");
        menu.setPath("/system");
        menu.setComponent("system/index");
        menu.setResourceType(ResourceTypeEnum.MENU.getCode());
        menu.setOpenWith(ResourceOpenWithEnum.INNER_CHAIN.getCode());
        menu.setLink("https://example.com");

        when(resourceService.findResourceListByApplicationId(eq(List.of(1L)), any())).thenReturn(List.of(menu));

        List<VueRouter> vbenTree = resourceBiz.findVisibleRouter(1L, 1001L, null, ClientTypeEnum.LAMP_WEB_PRO_VBEN5);
        assertEquals(1, vbenTree.size());
        assertEquals("https://example.com", vbenTree.get(0).getMeta().getIframeSrc());

        List<VueRouter> soybeanTree = resourceBiz.findVisibleRouter(1L, 1001L, null, ClientTypeEnum.LAMP_WEB_PRO_SOYBEAN);
        assertEquals(1, soybeanTree.size());

        List<VueRouter> normalTree = resourceBiz.findVisibleRouter(1L, 1001L, null, null);
        assertEquals(1, normalTree.size());

        // 4. findAllVisibleRouter 全应用路由树
        DefApplication app1 = new DefApplication();
        app1.setId(10L);
        app1.setName("App1");
        app1.setSortValue(1);
        app1.setRedirect("/system");
        when(appService.list(any())).thenReturn(List.of(app1));

        DefResource outerMenu = new DefResource();
        outerMenu.setId(2L);
        outerMenu.setParentId(0L);
        outerMenu.setName("Outer");
        outerMenu.setPath("/outer");
        outerMenu.setComponent("outer/index");
        outerMenu.setResourceType(ResourceTypeEnum.MENU.getCode());
        outerMenu.setOpenWith(ResourceOpenWithEnum.OUTER_CHAIN.getCode());
        outerMenu.setLink("https://google.com");
        outerMenu.setSubGroup("groupA");
        outerMenu.setMetaJson("{\"title\":\"OuterLink\"}");

        when(resourceService.findResourceListByApplicationId(eq(List.of(10L)), any())).thenReturn(List.of(menu, outerMenu));

        List<VueRouter> allVben = resourceBiz.findAllVisibleRouter(1001L, "groupA", ClientTypeEnum.LAMP_WEB_PRO_VBEN5);
        assertEquals(1, allVben.size());

        List<VueRouter> allSoybean = resourceBiz.findAllVisibleRouter(1001L, null, ClientTypeEnum.LAMP_WEB_PRO_SOYBEAN);
        assertEquals(1, allSoybean.size());

        List<VueRouter> allNormal = resourceBiz.findAllVisibleRouter(1001L, null, null);
        assertEquals(1, allNormal.size());

        // 5. checkEmployeeHaveApplication
        assertTrue(resourceBiz.checkEmployeeHaveApplication(1001L, 1L));
        assertFalse(resourceBiz.checkEmployeeHaveApplication(2001L, 1L));
    }

    @Test
    @DisplayName("测试 OauthUserBiz 用户信息聚合")
    void testOauthUserBiz() {
        BaseEmployeeService employeeService = Mockito.mock(BaseEmployeeService.class);
        DefUserService userService = Mockito.mock(DefUserService.class);
        DefApplicationService appService = Mockito.mock(DefApplicationService.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);

        OauthUserBiz userBiz = new OauthUserBiz(employeeService, userService, appService, appendixService);

        // 用户不存在
        when(userService.getByIdCache(999L)).thenReturn(null);
        assertNull(userBiz.getUserById(999L));

        // 用户存在
        DefUser user = new DefUser();
        user.setId(100L);
        user.setUsername("bob");
        when(userService.getByIdCache(100L)).thenReturn(user);

        AppendixResultVO avatar = new AppendixResultVO();
        avatar.setId(888L);
        when(appendixService.getByBiz(eq(100L), anyString())).thenReturn(avatar);

        DefApplication app = new DefApplication();
        app.setId(1L);
        app.setName("DefaultApp");
        when(appService.getDefApp(100L)).thenReturn(app);

        ContextUtil.setEmployeeId(200L);
        BaseEmployee emp = new BaseEmployee();
        emp.setId(200L);
        when(employeeService.getByIdCache(200L)).thenReturn(emp);

        DefUserInfoResultVO resultVO = userBiz.getUserById(100L);
        assertNotNull(resultVO);
        assertEquals(100L, resultVO.getId());
        assertEquals(888L, resultVO.getAvatarId());
        assertEquals(200L, resultVO.getEmployeeId());
        assertNotNull(resultVO.getDefApplication());
    }

    @Test
    @DisplayName("测试 StpInterfaceBiz 登录鉴权权限与角色列表提取")
    void testStpInterfaceBiz() {
        DefResourceService resourceService = Mockito.mock(DefResourceService.class);
        BaseRoleService roleService = Mockito.mock(BaseRoleService.class);

        StpInterfaceBiz stpBiz = new StpInterfaceBiz(resourceService, roleService);

        // 未登录时返回空
        assertTrue(stpBiz.getPermissionList().isEmpty());
        assertTrue(stpBiz.getRoleList().isEmpty());

        // 登录态模拟
        StpUtil.login(100L, "PC");
        SaSession session = StpUtil.getTokenSession();
        session.set(ContextConstants.JWT_KEY_EMPLOYEE_ID, 200L);

        // 租户管理员返回 *
        when(roleService.checkRole(200L, RoleConstant.TENANT_ADMIN)).thenReturn(true);
        assertEquals(List.of("*"), stpBiz.getPermissionList());
        assertEquals(List.of("*"), stpBiz.getRoleList());

        // 非管理员返回对应角色与资源
        when(roleService.checkRole(200L, RoleConstant.TENANT_ADMIN)).thenReturn(false);
        when(roleService.findRoleCodeByEmployeeId(200L)).thenReturn(List.of("ROLE_USER"));
        assertEquals(List.of("ROLE_USER"), stpBiz.getRoleList());

        // 无权限资源
        when(roleService.findResourceIdByEmployeeId(null, 200L)).thenReturn(Collections.emptyList());
        assertTrue(stpBiz.getPermissionList().isEmpty());

        // 有权限资源
        when(roleService.findResourceIdByEmployeeId(null, 200L)).thenReturn(List.of(10L));
        DefResource permRes = new DefResource();
        permRes.setId(10L);
        permRes.setCode("user:add;user:edit");
        when(resourceService.findByIdsAndType(eq(List.of(10L)), any())).thenReturn(List.of(permRes));
        List<String> perms = stpBiz.getPermissionList();
        assertEquals(2, perms.size());
        assertTrue(perms.contains("user:add"));

        // employeeId 为空返回空列表
        session.delete(ContextConstants.JWT_KEY_EMPLOYEE_ID);
        assertTrue(stpBiz.getPermissionList().isEmpty());
        assertTrue(stpBiz.getRoleList().isEmpty());
    }

    @Test
    @DisplayName("测试 ResourceBiz 菜单子视图隐藏与层级递归")
    void testResourceBizChildrenAllView() {
        DefResourceService resourceService = Mockito.mock(DefResourceService.class);
        DefApplicationService appService = Mockito.mock(DefApplicationService.class);
        BaseRoleService roleService = Mockito.mock(BaseRoleService.class);

        ResourceBiz resourceBiz = new ResourceBiz(resourceService, appService, roleService);
        when(roleService.checkRole(1001L, RoleConstant.TENANT_ADMIN)).thenReturn(true);

        DefResource parentMenu = new DefResource();
        parentMenu.setId(1L);
        parentMenu.setParentId(0L);
        parentMenu.setName("Parent");
        parentMenu.setPath("/parent");
        parentMenu.setComponent("parent/index");
        parentMenu.setResourceType(ResourceTypeEnum.MENU.getCode());

        DefResource childView = new DefResource();
        childView.setId(2L);
        childView.setParentId(1L);
        childView.setName("ChildView");
        childView.setPath("/parent/view");
        childView.setComponent("parent/view");
        childView.setResourceType(ResourceTypeEnum.MENU.getCode());
        childView.setIsHidden(true);

        when(resourceService.findResourceListByApplicationId(any(), any())).thenReturn(List.of(parentMenu, childView));

        List<VueRouter> vbenTree = resourceBiz.findVisibleRouter(1L, 1001L, null, ClientTypeEnum.LAMP_WEB_PRO_VBEN5);
        assertEquals(1, vbenTree.size());
        assertTrue(vbenTree.get(0).getMeta().getHideChildrenInMenu());

        List<VueRouter> soybeanTree = resourceBiz.findVisibleRouter(1L, 1001L, null, ClientTypeEnum.LAMP_WEB_PRO_SOYBEAN);
        assertEquals(1, soybeanTree.size());

        List<VueRouter> defaultTree = resourceBiz.findVisibleRouter(1L, 1001L, null, null);
        assertEquals(1, defaultTree.size());
    }
}
