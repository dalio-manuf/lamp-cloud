package com.dalio.cloud.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.cloud.msg.entity.DefInterface;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.manager.impl.DefInterfaceManagerImpl;
import com.dalio.cloud.msg.manager.impl.DefInterfacePropertyManagerImpl;
import com.dalio.cloud.msg.manager.impl.DefMsgTemplateManagerImpl;
import com.dalio.cloud.system.biz.application.DefResourceBiz;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.entity.application.DefUserApplication;
import com.dalio.cloud.system.entity.system.DefClient;
import com.dalio.cloud.system.entity.system.DefParameter;
import com.dalio.cloud.system.entity.tenant.DefDatasourceConfig;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.manager.application.DefUserApplicationManager;
import com.dalio.cloud.system.manager.application.impl.DefApplicationManagerImpl;
import com.dalio.cloud.system.manager.application.impl.DefUserApplicationManagerImpl;
import com.dalio.cloud.system.manager.system.impl.DefAreaManagerImpl;
import com.dalio.cloud.system.manager.system.impl.DefClientManagerImpl;
import com.dalio.cloud.system.manager.system.impl.DefParameterManagerImpl;
import com.dalio.cloud.system.manager.tenant.DefUserManager;
import com.dalio.cloud.system.manager.tenant.impl.DefDatasourceConfigManagerImpl;
import com.dalio.cloud.system.mapper.application.DefApplicationMapper;
import com.dalio.cloud.system.service.application.DefResourceService;
import com.dalio.cloud.system.service.application.impl.DefUserApplicationServiceImpl;
import com.dalio.cloud.system.service.tenant.DefUserService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 系统模块未覆盖 Manager 与 Service 单元测试
 */
class SystemManagerAndServiceExtrasTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefDatasourceConfig.class);
        TableInfoHelper.initTableInfo(assistant, DefMsgTemplate.class);
        TableInfoHelper.initTableInfo(assistant, DefInterface.class);
        TableInfoHelper.initTableInfo(assistant, DefInterfaceProperty.class);
        TableInfoHelper.initTableInfo(assistant, DefApplication.class);
        TableInfoHelper.initTableInfo(assistant, DefClient.class);
        TableInfoHelper.initTableInfo(assistant, DefParameter.class);
        TableInfoHelper.initTableInfo(assistant, DefUserApplication.class);
    }

    @Test
    @DisplayName("测试 DefDatasourceConfigManagerImpl")
    void testDefDatasourceConfigManagerImpl() {
        DefDatasourceConfigManagerImpl mgr = spy(new DefDatasourceConfigManagerImpl());
        DefDatasourceConfig cfg = new DefDatasourceConfig();
        cfg.setName("testDs");
        doReturn(cfg).when(mgr).getOne(any(), eq(false));
        assertEquals(cfg, mgr.getByName("testDs"));
    }

    @Test
    @DisplayName("测试 DefUserService default 方法")
    void testDefUserServiceDefaultMethods() throws Throwable {
        DefUserManager userManager = mock(DefUserManager.class);
        doReturn(List.of(new DefUser())).when(userManager).listByIds(any());
        doReturn(List.of(new DefUser())).when(userManager).listByIds(anyList());
        doReturn(List.of(new DefUser())).when(userManager).listByIds(anyCollection());

        java.lang.reflect.Method method = DefUserService.class.getMethod("listByIds", java.util.Collection.class);
        assertTrue(method.isDefault());

        com.dalio.cloud.system.service.tenant.impl.DefUserServiceImpl serviceImpl =
                new com.dalio.cloud.system.service.tenant.impl.DefUserServiceImpl(null, null);
        ReflectionTestUtils.setField(serviceImpl, "superManager", userManager);

        java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
        java.lang.invoke.MethodHandle mh = java.lang.invoke.MethodHandles.privateLookupIn(DefUserService.class, lookup)
                .findSpecial(DefUserService.class, "listByIds",
                        java.lang.invoke.MethodType.methodType(List.class, java.util.Collection.class),
                        DefUserService.class);

        @SuppressWarnings("unchecked")
        List<DefUser> result = (List<DefUser>) mh.bindTo(serviceImpl).invoke(List.of(1L));
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试 DefUserApplicationServiceImpl")
    void testDefUserApplicationServiceImpl() {
        DefUserApplicationServiceImpl userAppService = new DefUserApplicationServiceImpl();
        DefUserApplicationManager userAppMgr = mock(DefUserApplicationManager.class);
        ReflectionTestUtils.setField(userAppService, "superManager", userAppMgr);

        DefUserApplication ua = new DefUserApplication();
        ua.setApplicationId(999L);
        when(userAppMgr.getOne(any(), eq(false))).thenReturn(ua);
        assertEquals(999L, userAppService.getMyDefAppByUserId(1L));

        when(userAppMgr.getOne(any(), eq(false))).thenReturn(null);
        assertNull(userAppService.getMyDefAppByUserId(2L));
    }

    @Test
    @DisplayName("测试 消息与接口通用Manager")
    void testMsgAndInterfaceManagers() {
        // DefMsgTemplateManagerImpl
        DefMsgTemplateManagerImpl msgTplMgr = spy(new DefMsgTemplateManagerImpl());
        DefMsgTemplate tpl = new DefMsgTemplate();
        tpl.setCode("CODE1");
        doReturn(tpl).when(msgTplMgr).getOne(any());
        assertEquals(tpl, msgTplMgr.getByCode("CODE1"));

        // DefInterfaceManagerImpl
        DefInterfaceManagerImpl ifaceMgr = spy(new DefInterfaceManagerImpl());
        DefInterface iface = new DefInterface();
        iface.setCode("TYPE1");
        doReturn(iface).when(ifaceMgr).getOne(any());
        assertEquals(iface, ifaceMgr.getByType("TYPE1"));

        // DefInterfacePropertyManagerImpl
        DefInterfacePropertyManagerImpl propMgr = spy(new DefInterfacePropertyManagerImpl());
        DefInterfaceProperty prop = new DefInterfaceProperty();
        prop.setKey("k1");
        prop.setValue("v1");
        doReturn(List.of(prop)).when(propMgr).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        Map<String, Object> map = propMgr.listByInterfaceId(10L);
        assertEquals("v1", map.get("k1"));
    }

    @Test
    @DisplayName("测试 DefResourceBiz 缓存与关联删除")
    void testDefResourceBiz() {
        DefResourceService resService = mock(DefResourceService.class);
        DefResourceBiz resBiz = new DefResourceBiz(resService);
        when(resService.removeByIdWithCache(anyList())).thenReturn(true);
        assertTrue(resBiz.removeByIdWithCache(List.of(1L, 2L)));
        verify(resService).deleteRoleResourceRelByResourceId(List.of(1L, 2L));
    }

    @Test
    @DisplayName("测试 DefApplicationManagerImpl 与 DefUserApplicationManagerImpl")
    void testApplicationManagers() {
        assertNotNull(new DefUserApplicationManagerImpl());

        DefApplicationManagerImpl appMgr = spy(new DefApplicationManagerImpl());
        DefApplicationMapper appMapper = mock(DefApplicationMapper.class);
        ReflectionTestUtils.setField(appMgr, "baseMapper", appMapper);

        assertNotNull(ReflectionTestUtils.invokeMethod(appMgr, "cacheKeyBuilder"));

        DefApplication app = new DefApplication();
        app.setId(10L);
        app.setName("App1");
        doReturn(List.of(app)).when(appMgr).findByIds(anySet(), any());

        Map<Serializable, Object> appMap = appMgr.findByIds(Set.of(10L));
        assertEquals("App1", appMap.get(10L));

        when(appMapper.findMyApplication("test")).thenReturn(Collections.emptyList());
        assertNotNull(appMgr.findMyApplication("test"));
        assertNotNull(appMgr.findRecommendApplication("test"));

        doReturn(List.of(app)).when(appMgr).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        assertEquals(1, appMgr.findGeneral().size());
    }

    @Test
    @DisplayName("测试 DefClientManagerImpl")
    void testDefClientManagerImpl() {
        DefClientManagerImpl clientMgr = spy(new DefClientManagerImpl());
        CacheOps cacheOps = mock(CacheOps.class);
        ReflectionTestUtils.setField(clientMgr, "cacheOps", cacheOps);

        assertNotNull(ReflectionTestUtils.invokeMethod(clientMgr, "cacheKeyBuilder"));

        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class), any(java.util.function.Function.class)))
                .thenReturn(new CacheResult<>("k", 100L));
        DefClient client = new DefClient();
        client.setId(100L);
        doReturn(client).when(clientMgr).getByIdCache(100L);

        DefClient fetched = clientMgr.getClient("cid", "secret");
        assertEquals(100L, fetched.getId());
    }

    @Test
    @DisplayName("测试 DefParameterManagerImpl 与 DefAreaManagerImpl")
    void testParameterAndAreaManagers() {
        assertNotNull(new DefAreaManagerImpl());

        DefParameterManagerImpl paramMgr = spy(new DefParameterManagerImpl());
        assertNotNull(ReflectionTestUtils.invokeMethod(paramMgr, "cacheKeyBuilder"));

        assertTrue(paramMgr.findByIds(Collections.emptySet()).isEmpty());

        DefParameter p1 = new DefParameter();
        p1.setId(1L);
        p1.setName("Param1");
        p1.setKey("key1");
        p1.setValue("val1");

        doReturn(List.of(p1)).when(paramMgr).listByIds(anyList());
        Map<Serializable, Object> pMap = paramMgr.findByIds(Set.of(1L));
        assertEquals("Param1", pMap.get(1L));

        assertTrue(paramMgr.findParamMapByKey(Collections.emptyList()).isEmpty());
        doReturn(List.of(p1)).when(paramMgr).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        Map<String, String> pKeyMap = paramMgr.findParamMapByKey(List.of("key1"));
        assertEquals("val1", pKeyMap.get("key1"));
    }
}
