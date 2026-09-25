package com.dalio.cloud.system.controller.tenant;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dalio.basic.base.R;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.common.constant.AppendixType;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.system.controller.anyone.SystemEchoController;
import com.dalio.cloud.system.controller.anyone.TenantAnyoneController;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.application.DefApplicationService;
import com.dalio.cloud.system.service.tenant.DefDatasourceConfigService;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.system.vo.query.system.OnlineUsersPageQuery;
import com.dalio.cloud.system.vo.query.tenant.DefUserPageQuery;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;
import com.dalio.cloud.system.vo.result.system.OnlineTokenResultVO;
import com.dalio.cloud.system.vo.result.system.OnlineUsersResultVO;
import com.dalio.cloud.system.vo.result.tenant.DefUserExcelVO;
import com.dalio.cloud.system.vo.result.tenant.DefUserResultVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserPasswordResetVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
 * DefDatasourceConfigController, SystemEchoController, TenantAnyoneController 与 DefUserController 单元测试
 */
class DefTenantControllersTest {

    @Test
    @DisplayName("测试 DefDatasourceConfigController 接口与测试连接")
    void testDefDatasourceConfigController() {
        DefDatasourceConfigService service = mock(DefDatasourceConfigService.class);
        EchoService echoService = mock(EchoService.class);
        DefDatasourceConfigController controller = new DefDatasourceConfigController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());

        when(service.testConnection(1L)).thenReturn(true);
        R<Boolean> testR = controller.testConnect(1L);
        assertTrue(testR.getIsSuccess());
        assertTrue(testR.getData());
    }

    @Test
    @DisplayName("测试 SystemEchoController 数据注入查询")
    void testSystemEchoController() {
        DefUserService defUserService = mock(DefUserService.class);
        SystemEchoController controller = new SystemEchoController(defUserService);

        when(defUserService.findByIds(Set.of(1L, 2L))).thenReturn(Map.of(1L, "user1", 2L, "user2"));
        Map<Serializable, Object> res = controller.findUserByIds(Set.of(1L, 2L));
        assertEquals(2, res.size());
        assertEquals("user1", res.get(1L));
    }

    @Test
    @DisplayName("测试 TenantAnyoneController 应用查询与默认应用设置")
    void testTenantAnyoneController() {
        DefApplicationService appService = mock(DefApplicationService.class);
        EchoService echoService = mock(EchoService.class);
        AppendixService appendixService = mock(AppendixService.class);
        TenantAnyoneController controller = new TenantAnyoneController(appService, echoService, appendixService);

        ContextUtil.setUserId(100L);
        try {
            when(appService.updateDefApp(10L, 100L)).thenReturn(true);
            R<Boolean> updateR = controller.updateDefApp(10L);
            assertTrue(updateR.getIsSuccess());
            assertTrue(updateR.getData());

            DefApplication app = new DefApplication();
            app.setId(10L);
            when(appService.getDefApp(100L)).thenReturn(app);
            R<DefApplication> getR = controller.getDefApp();
            assertTrue(getR.getIsSuccess());
            assertEquals(10L, getR.getData().getId());

            List<DefApplicationResultVO> list1 = List.of(new DefApplicationResultVO());
            when(appService.findMyApplication("test")).thenReturn(list1);
            R<List<DefApplicationResultVO>> myApps = controller.findMyApplication("test");
            assertTrue(myApps.getIsSuccess());

            List<DefApplicationResultVO> list2 = List.of(new DefApplicationResultVO());
            when(appService.findRecommendApplication("rec")).thenReturn(list2);
            R<List<DefApplicationResultVO>> recApps = controller.findRecommendApplication("rec");
            assertTrue(recApps.getIsSuccess());

            verify(echoService, Mockito.times(2)).action(anyList());
            verify(appendixService, Mockito.times(2)).echoAppendix(anyList(), eq(AppendixType.System.DEF__APPLICATION__LOGO));
        } finally {
            ContextUtil.setUserId(null);
        }
    }

    @Test
    @DisplayName("测试 DefUserController 基础属性、用户检索与状态操作")
    void testDefUserControllerBasicAndCRUD() {
        DefUserService service = mock(DefUserService.class);
        EchoService echoService = mock(EchoService.class);
        DefUserController controller = new DefUserController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        assertSame(echoService, controller.getEchoService());
        assertEquals(DefUserExcelVO.class, controller.getExcelClass());

        // buildPager test
        IPage<String> p0 = DefUserController.buildPager(10, 1, Collections.emptyList());
        assertTrue(p0.getRecords().isEmpty());

        IPage<String> p1 = DefUserController.buildPager(10, 5, List.of("a", "b"));
        assertTrue(p1.getRecords().isEmpty());

        IPage<String> p2 = DefUserController.buildPager(2, 1, List.of("a", "b", "c"));
        assertEquals(2, p2.getRecords().size());

        // check methods
        when(service.checkUsername("user", 1L)).thenReturn(true);
        assertTrue(controller.checkUsername("user", 1L).getData());

        when(service.checkEmail("email@test.com", 1L)).thenReturn(true);
        assertTrue(controller.checkEmail("email@test.com", 1L).getData());

        when(service.checkIdCard("card", 1L)).thenReturn(true);
        assertTrue(controller.checkIdCard("card", 1L).getData());

        when(service.checkMobile("13800000000", 1L)).thenReturn(true);
        assertTrue(controller.checkMobile("13800000000", 1L).getData());

        // resetPassword & updateState
        DefUserPasswordResetVO resetVO = new DefUserPasswordResetVO();
        resetVO.setId(1L);
        when(service.resetPassword(resetVO)).thenReturn(true);
        assertTrue(controller.resetPassword(resetVO).getData());

        when(service.updateState(1L, true)).thenReturn(true);
        assertTrue(controller.updateState(1L, true).getData());

        // findAllUserId
        when(service.findUserIdList(null)).thenReturn(List.of(1L, 2L));
        assertEquals(2, controller.findAllUserId().getData().size());

        // pageUser
        PageParams<DefUserPageQuery> pageParams = new PageParams<>();
        pageParams.setCurrent(1);
        pageParams.setSize(10);
        pageParams.setModel(new DefUserPageQuery());
        IPage<DefUserResultVO> page = new Page<>();
        when(service.pageUser(pageParams)).thenReturn(page);
        R<IPage<DefUserResultVO>> pageR = controller.pageUser(pageParams);
        assertTrue(pageR.getIsSuccess());
        verify(echoService).action(page);

        // queryUser
        when(service.queryUser(any())).thenReturn(List.of(new DefUserResultVO()));
        assertEquals(1, controller.queryUser(new DefUserPageQuery()).getData().size());
    }

    @Test
    @DisplayName("测试 DefUserController 在线人员注销、踢出、分页与 Token 签名查询")
    void testDefUserControllerOnlineUsersAndToken() {
        DefUserService service = mock(DefUserService.class);
        EchoService echoService = mock(EchoService.class);
        DefUserController controller = new DefUserController(echoService);
        ReflectionTestUtils.setField(controller, "superService", service);

        cn.dev33.satoken.dao.SaTokenDao oldDao = cn.dev33.satoken.SaManager.getSaTokenDao();
        cn.dev33.satoken.dao.SaTokenDao mockDao = mock(cn.dev33.satoken.dao.SaTokenDao.class);
        when(mockDao.getSessionTimeout(any())).thenReturn(3600L);
        when(mockDao.getObjectTimeout(any())).thenReturn(3600L);
        cn.dev33.satoken.SaManager.setSaTokenDao(mockDao);

        try (MockedStatic<StpUtil> mockedStp = Mockito.mockStatic(StpUtil.class)) {
            // logout
            controller.logout(1L, "token1");
            mockedStp.verify(() -> StpUtil.logout(1L));
            mockedStp.verify(() -> StpUtil.logoutByTokenValue("token1"));

            controller.logout(null, null);

            // kickout
            controller.kickout(2L, "token2");
            mockedStp.verify(() -> StpUtil.kickout(2L));
            mockedStp.verify(() -> StpUtil.kickoutByTokenValue("token2"));

            controller.kickout(null, null);

            // onlineUsersPage
            mockedStp.when(() -> StpUtil.searchSessionId(StringPool.EMPTY, 0, -1, false))
                    .thenReturn(List.of("session_null", "session_user1", "session_user2", "session_nouser"));

            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_null")).thenReturn(null);

            SaSession sessionUser1 = mock(SaSession.class);
            when(sessionUser1.getId()).thenReturn("session_user1");
            when(sessionUser1.getLoginId()).thenReturn("101");
            when(sessionUser1.getCreateTime()).thenReturn(System.currentTimeMillis() - 60000);
            when(sessionUser1.timeout()).thenReturn(3600L);
            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_user1")).thenReturn(sessionUser1);

            DefUser user1 = new DefUser();
            user1.setId(101L);
            user1.setUsername("zhangsan");
            user1.setNickName("张三");
            when(service.getByIdCache(101L)).thenReturn(user1);

            SaSession sessionUser2 = mock(SaSession.class);
            when(sessionUser2.getId()).thenReturn("session_user2");
            when(sessionUser2.getLoginId()).thenReturn("102");
            when(sessionUser2.getCreateTime()).thenReturn(System.currentTimeMillis() - 30000);
            when(sessionUser2.timeout()).thenReturn(3600L);
            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_user2")).thenReturn(sessionUser2);

            DefUser user2 = new DefUser();
            user2.setId(102L);
            user2.setUsername("lisi");
            user2.setNickName("李四");
            when(service.getByIdCache(102L)).thenReturn(user2);

            SaSession sessionNoUser = mock(SaSession.class);
            when(sessionNoUser.getId()).thenReturn("session_nouser");
            when(sessionNoUser.getLoginId()).thenReturn("103");
            when(sessionNoUser.getCreateTime()).thenReturn(System.currentTimeMillis() - 10000);
            when(sessionNoUser.timeout()).thenReturn(3600L);
            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_nouser")).thenReturn(sessionNoUser);
            when(service.getByIdCache(103L)).thenReturn(null);

            // 1. 无过滤查询
            OnlineUsersPageQuery q1 = new OnlineUsersPageQuery();
            PageParams<OnlineUsersPageQuery> p1 = new PageParams<>();
            p1.setCurrent(1);
            p1.setSize(10);
            p1.setModel(q1);
            R<IPage<OnlineUsersResultVO>> pageR1 = controller.onlineUsersPage(p1);
            assertTrue(pageR1.getIsSuccess());
            assertEquals(3, pageR1.getData().getRecords().size());

            // 2. 带有 username 过滤
            OnlineUsersPageQuery q2 = new OnlineUsersPageQuery();
            q2.setUsername("zhang");
            PageParams<OnlineUsersPageQuery> p2 = new PageParams<>();
            p2.setCurrent(1);
            p2.setSize(10);
            p2.setModel(q2);
            R<IPage<OnlineUsersResultVO>> pageR2 = controller.onlineUsersPage(p2);
            assertEquals(2, pageR2.getData().getRecords().size());

            // 3. 带有 nickName 过滤
            OnlineUsersPageQuery q3 = new OnlineUsersPageQuery();
            q3.setNickName("李");
            PageParams<OnlineUsersPageQuery> p3 = new PageParams<>();
            p3.setCurrent(1);
            p3.setSize(10);
            p3.setModel(q3);
            R<IPage<OnlineUsersResultVO>> pageR3 = controller.onlineUsersPage(p3);
            assertEquals(2, pageR3.getData().getRecords().size());

            // getTokenSignList
            // 1. session 为空
            OnlineUsersPageQuery signQ1 = new OnlineUsersPageQuery();
            signQ1.setSessionId("session_none");
            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_none")).thenReturn(null);
            PageParams<OnlineUsersPageQuery> signParams1 = new PageParams<>();
            signParams1.setCurrent(1);
            signParams1.setSize(10);
            signParams1.setModel(signQ1);
            R<IPage<OnlineTokenResultVO>> signR1 = controller.getTokenSignList(signParams1);
            assertTrue(signR1.getIsSuccess());
            assertTrue(signR1.getData().getRecords().isEmpty());

            // 2. session 存在，包含有效 token 和异常 token
            SaSession signSession = mock(SaSession.class);
            SaTerminalInfo term1 = new SaTerminalInfo();
            term1.setTokenValue("valid_token");
            SaTerminalInfo term2 = new SaTerminalInfo();
            term2.setTokenValue("err_token");
            when(signSession.getTerminalList()).thenReturn(List.of(term1, term2));

            SaSession tokenSession = mock(SaSession.class);
            when(tokenSession.getCreateTime()).thenReturn(System.currentTimeMillis() - 20000);
            when(tokenSession.timeout()).thenReturn(1800L);
            mockedStp.when(() -> StpUtil.getTokenSessionByToken("valid_token")).thenReturn(tokenSession);
            mockedStp.when(() -> StpUtil.getTokenSessionByToken("err_token")).thenThrow(new RuntimeException("token expired"));

            mockedStp.when(() -> StpUtil.getSessionBySessionId("session_sign")).thenReturn(signSession);
            OnlineUsersPageQuery signQ2 = new OnlineUsersPageQuery();
            signQ2.setSessionId("session_sign");
            PageParams<OnlineUsersPageQuery> signParams2 = new PageParams<>();
            signParams2.setCurrent(1);
            signParams2.setSize(10);
            signParams2.setModel(signQ2);
            R<IPage<OnlineTokenResultVO>> signR2 = controller.getTokenSignList(signParams2);
            assertTrue(signR2.getIsSuccess());
            assertEquals(2, signR2.getData().getRecords().size());
        } finally {
            cn.dev33.satoken.SaManager.setSaTokenDao(oldDao);
        }
    }
}
