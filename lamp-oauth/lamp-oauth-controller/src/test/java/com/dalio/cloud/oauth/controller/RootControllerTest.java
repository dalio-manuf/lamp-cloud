package com.dalio.cloud.oauth.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.enumeration.GrantType;
import com.dalio.cloud.oauth.granter.RefreshTokenGranter;
import com.dalio.cloud.oauth.granter.TokenGranter;
import com.dalio.cloud.oauth.granter.TokenGranterBuilder;
import com.dalio.cloud.oauth.service.UserInfoService;
import com.dalio.cloud.oauth.vo.param.LoginParamVO;
import com.dalio.cloud.oauth.vo.param.RegisterByEmailVO;
import com.dalio.cloud.oauth.vo.param.RegisterByMobileVO;
import com.dalio.cloud.oauth.vo.result.LoginResultVO;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.system.vo.query.tenant.ForgetPasswordDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RootController 单元测试
 */
class RootControllerTest {

    @Test
    @DisplayName("测试 RootController 登录、刷新、切部门、登出")
    void testAuthLifecycle() {
        TokenGranterBuilder tokenGranterBuilder = Mockito.mock(TokenGranterBuilder.class);
        RefreshTokenGranter refreshTokenGranter = Mockito.mock(RefreshTokenGranter.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        TokenGranter mockGranter = Mockito.mock(TokenGranter.class);

        RootController controller = new RootController(tokenGranterBuilder, refreshTokenGranter, defUserService, userInfoService);

        // login
        LoginParamVO loginParam = new LoginParamVO();
        loginParam.setGrantType(GrantType.PASSWORD);
        when(tokenGranterBuilder.getGranter(GrantType.PASSWORD)).thenReturn(mockGranter);
        when(mockGranter.login(loginParam)).thenReturn(R.success(LoginResultVO.builder().token("tok1").build()));

        R<LoginResultVO> loginR = controller.login(loginParam);
        assertTrue(loginR.getIsSuccess());
        assertEquals("tok1", loginR.getData().getToken());

        // refresh
        when(refreshTokenGranter.refresh("ref-tok")).thenReturn(LoginResultVO.builder().token("tok2").build());
        R<LoginResultVO> refreshR = controller.refresh("ref-tok");
        assertTrue(refreshR.getIsSuccess());
        assertEquals("tok2", refreshR.getData().getToken());

        // switchOrg
        when(mockGranter.switchOrg(10L)).thenReturn(LoginResultVO.builder().token("tok3").build());
        R<LoginResultVO> switchR = controller.switchOrg(10L);
        assertTrue(switchR.getIsSuccess());
        assertEquals("tok3", switchR.getData().getToken());

        // logout
        when(tokenGranterBuilder.getGranter()).thenReturn(mockGranter);
        when(mockGranter.logout()).thenReturn(R.success(true));
        R<Boolean> logoutR = controller.logout();
        assertTrue(logoutR.getIsSuccess());
        assertTrue(logoutR.getData());
    }

    @Test
    @DisplayName("测试 RootController verify token 校验")
    void testVerify() {
        TokenGranterBuilder tokenGranterBuilder = Mockito.mock(TokenGranterBuilder.class);
        RefreshTokenGranter refreshTokenGranter = Mockito.mock(RefreshTokenGranter.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);

        RootController controller = new RootController(tokenGranterBuilder, refreshTokenGranter, defUserService, userInfoService);

        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            SaSession session = new SaSession();
            stpMock.when(() -> StpUtil.getTokenSessionByToken("valid-token")).thenReturn(session);

            R<SaSession> verifyR = controller.verify("valid-token");
            assertTrue(verifyR.getIsSuccess());
            assertNotNull(verifyR.getData());
            stpMock.verify(() -> StpUtil.getTokenSessionByToken("valid-token"));
        }
    }

    @Test
    @DisplayName("测试 RootController 注册、检测手机号、忘记密码")
    void testRegistrationAndAccount() {
        TokenGranterBuilder tokenGranterBuilder = Mockito.mock(TokenGranterBuilder.class);
        RefreshTokenGranter refreshTokenGranter = Mockito.mock(RefreshTokenGranter.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);

        RootController controller = new RootController(tokenGranterBuilder, refreshTokenGranter, defUserService, userInfoService);

        // registerByMobile
        RegisterByMobileVO regMobile = new RegisterByMobileVO();
        regMobile.setMobile("13800000000");
        when(userInfoService.registerByMobile(regMobile)).thenReturn("success-mobile");
        R<String> regMobR = controller.register(regMobile);
        assertTrue(regMobR.getIsSuccess());
        assertEquals("success-mobile", regMobR.getData());

        // registerByEmail
        RegisterByEmailVO regEmail = new RegisterByEmailVO();
        regEmail.setEmail("test@email.com");
        when(userInfoService.registerByEmail(regEmail)).thenReturn("success-email");
        R<String> regEmailR = controller.register(regEmail);
        assertTrue(regEmailR.getIsSuccess());
        assertEquals("success-email", regEmailR.getData());

        // checkMobile
        when(defUserService.checkMobile("13800000000", null)).thenReturn(true);
        R<Boolean> checkMobR = controller.checkMobile("13800000000");
        assertTrue(checkMobR.getIsSuccess());
        assertTrue(checkMobR.getData());

        // forgetPassword
        ForgetPasswordDto dto = new ForgetPasswordDto();
        when(defUserService.forgetPassword(dto)).thenReturn(R.success(true));
        R<Boolean> forgetR = controller.forgetPassword(dto);
        assertTrue(forgetR.getIsSuccess());
        assertTrue(forgetR.getData());
        verify(defUserService).forgetPassword(dto);
    }
}
