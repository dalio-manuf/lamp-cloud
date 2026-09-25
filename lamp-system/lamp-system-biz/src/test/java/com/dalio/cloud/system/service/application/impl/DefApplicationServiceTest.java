package com.dalio.cloud.system.service.application.impl;

import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.manager.application.DefApplicationManager;
import com.dalio.cloud.system.manager.application.DefResourceManager;
import com.dalio.cloud.system.manager.application.DefUserApplicationManager;
import com.dalio.cloud.system.vo.result.application.ApplicationResourceResultVO;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;
import com.dalio.cloud.system.vo.save.application.DefApplicationSaveVO;
import com.dalio.cloud.system.vo.update.application.DefApplicationUpdateVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefApplicationServiceTest {

    @Test
    @DisplayName("测试 findMyApplication 有效期计算")
    void testFindMyApplication() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);

        DefApplicationServiceImpl service = new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager);
        ReflectionTestUtils.setField(service, "superManager", appManager);

        DefApplicationResultVO vo1 = new DefApplicationResultVO();
        vo1.setName("App1");
        vo1.setExpirationTime(LocalDateTime.now().plusDays(5));

        DefApplicationResultVO vo2 = new DefApplicationResultVO();
        vo2.setName("App2");
        vo2.setExpirationTime(LocalDateTime.now().minusDays(1));

        when(appManager.findMyApplication("test")).thenReturn(List.of(vo1, vo2));

        List<DefApplicationResultVO> result = service.findMyApplication("test");
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getState()); // 有效
        assertEquals("0", result.get(1).getState()); // 过期
    }

    @Test
    @DisplayName("测试 check 重名检测与 save/updateById 冲突防御")
    void testCheckAndSaveUpdate() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);

        DefApplicationServiceImpl service = new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager);
        ReflectionTestUtils.setField(service, "superManager", appManager);

        when(appManager.count(any())).thenReturn(1L);

        // 重名时 save 抛出异常
        DefApplicationSaveVO saveVO = new DefApplicationSaveVO();
        saveVO.setName("重复应用");
        assertThrows(RuntimeException.class, () -> service.save(saveVO));

        // 重名时 update 抛出异常
        DefApplicationUpdateVO updateVO = new DefApplicationUpdateVO();
        updateVO.setId(10L);
        updateVO.setName("重复应用");
        assertThrows(RuntimeException.class, () -> service.updateById(updateVO));

        // 不重名时正常 save
        when(appManager.count(any())).thenReturn(0L);
        doAnswer(invocation -> {
            DefApplication app = invocation.getArgument(0);
            app.setId(100L);
            return true;
        }).when(appManager).save(any(DefApplication.class));

        DefApplication created = service.save(saveVO);
        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertNotNull(created.getAppKey());
        assertNotNull(created.getAppSecret());
        verify(appManager).save(any(DefApplication.class));
    }

    @Test
    @DisplayName("测试 findAvailableApplicationResourceList 树形构建")
    void testFindAvailableApplicationResourceList() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);

        DefApplicationServiceImpl service = new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager);
        ReflectionTestUtils.setField(service, "superManager", appManager);

        DefApplication app = new DefApplication();
        app.setId(100L);
        app.setName("基础服务");
        when(appManager.list()).thenReturn(List.of(app));

        DefResource res = new DefResource();
        res.setId(1L);
        res.setApplicationId(100L);
        res.setName("菜单A");
        res.setParentId(0L);

        when(resManager.findResourceListByApplicationId(anyList(), anyCollection())).thenReturn(List.of(res));

        List<ApplicationResourceResultVO> result = service.findAvailableApplicationResourceList();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("基础服务", result.get(0).getDefApplication().getName());
        verify(echoService).action(anyList());
    }

    @Test
    @DisplayName("测试 findRecommendApplication 与 findApplicationResourceList")
    void testFindRecommendAndAppResourceList() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);

        DefApplicationServiceImpl service = Mockito.spy(new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager));
        ReflectionTestUtils.setField(service, "superManager", appManager);

        when(appManager.findRecommendApplication("rec")).thenReturn(List.of(new DefApplicationResultVO()));
        assertEquals(1, service.findRecommendApplication("rec").size());

        DefApplication app = new DefApplication();
        app.setId(10L);
        doReturn(List.of(app)).when(service).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        DefResource res = new DefResource();
        res.setId(1L);
        res.setApplicationId(10L);
        res.setParentId(0L);
        when(resManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(res));

        List<ApplicationResourceResultVO> list = service.findApplicationResourceList();
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("测试 findAvailableApplicationDataScopeList 数据权限树构建")
    void testFindAvailableApplicationDataScopeList() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);

        DefApplicationServiceImpl service = new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager);
        ReflectionTestUtils.setField(service, "superManager", appManager);

        DefApplication app = new DefApplication();
        app.setId(100L);
        when(appManager.list()).thenReturn(List.of(app));

        DefResource dataScope = new DefResource();
        dataScope.setId(10L);
        dataScope.setApplicationId(100L);
        dataScope.setParentId(0L);
        dataScope.setTreePath("0,");

        when(resManager.findResourceListByApplicationId(anyList(), anyList())).thenReturn(List.of(dataScope));
        when(resManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(dataScope));

        List<ApplicationResourceResultVO> res = service.findAvailableApplicationDataScopeList();
        assertEquals(1, res.size());
    }

    @Test
    @DisplayName("测试 updateDefApp 与 getDefApp 与 removeByIds")
    void testDefAppAndRemove() {
        DefResourceManager resManager = mock(DefResourceManager.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        DefUserApplicationManager userAppManager = mock(DefUserApplicationManager.class);
        DefApplicationManager appManager = mock(DefApplicationManager.class);
        com.dalio.basic.cache.repository.CacheOps cacheOps = mock(com.dalio.basic.cache.repository.CacheOps.class);

        DefApplicationServiceImpl service = Mockito.spy(new DefApplicationServiceImpl(resManager, echoService, appendixService, userAppManager));
        ReflectionTestUtils.setField(service, "superManager", appManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. updateDefApp
        when(userAppManager.save(any(com.dalio.cloud.system.entity.application.DefUserApplication.class))).thenReturn(true);
        assertTrue(service.updateDefApp(100L, 1L));
        verify(userAppManager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        // 2. getDefApp
        when(userAppManager.getOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class), eq(false))).thenReturn(null);
        assertNull(service.getDefApp(1L));

        com.dalio.cloud.system.entity.application.DefUserApplication userApp = new com.dalio.cloud.system.entity.application.DefUserApplication();
        userApp.setApplicationId(100L);
        when(userAppManager.getOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class), eq(false))).thenReturn(userApp);
        when(appManager.getByIdCache(100L)).thenReturn(new DefApplication());
        assertNotNull(service.getDefApp(1L));

        // 3. removeByIds
        when(appManager.removeByIds(anyCollection())).thenReturn(true);
        assertTrue(service.removeByIds(List.of(100L)));
        verify(cacheOps).del(any(com.dalio.basic.model.cache.CacheKey[].class));
        verify(appendixService).removeByBizId(anyCollection(), any());
    }
}
