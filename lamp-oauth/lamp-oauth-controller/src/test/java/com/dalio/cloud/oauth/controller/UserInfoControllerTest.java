package com.dalio.cloud.oauth.controller;

import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.oauth.biz.OauthUserBiz;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.service.UserInfoService;
import com.dalio.cloud.oauth.vo.result.DefUserInfoResultVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.system.vo.update.tenant.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserInfoController 单元测试
 */
class UserInfoControllerTest {

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    @DisplayName("测试 getUserInfoById 显式传入与上下文回退")
    void testGetUserInfoById() {
        OauthUserBiz oauthUserBiz = Mockito.mock(OauthUserBiz.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);

        UserInfoController controller = new UserInfoController(oauthUserBiz, userInfoService, defUserService, captchaService);

        // 显式传入 userId
        when(oauthUserBiz.getUserById(100L)).thenReturn(DefUserInfoResultVO.builder().id(100L).username("u100").build());
        R<DefUserInfoResultVO> r1 = controller.getUserInfoById(100L);
        assertTrue(r1.getIsSuccess());
        assertEquals("u100", r1.getData().getUsername());

        // userId == null, 从 ContextUtil 回退
        ContextUtil.set(ContextConstants.USER_ID_HEADER, 200L);
        when(oauthUserBiz.getUserById(200L)).thenReturn(DefUserInfoResultVO.builder().id(200L).username("u200").build());
        R<DefUserInfoResultVO> r2 = controller.getUserInfoById(null);
        assertTrue(r2.getIsSuccess());
        assertEquals("u200", r2.getData().getUsername());
    }

    @Test
    @DisplayName("测试 avatar, password, baseInfo 更新接口")
    void testBasicUpdates() {
        OauthUserBiz oauthUserBiz = Mockito.mock(OauthUserBiz.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);

        UserInfoController controller = new UserInfoController(oauthUserBiz, userInfoService, defUserService, captchaService);

        // avatar
        DefUserAvatarUpdateVO avatarVO = new DefUserAvatarUpdateVO();
        when(defUserService.updateAvatar(avatarVO)).thenReturn(true);
        R<Boolean> avR = controller.avatar(avatarVO);
        assertTrue(avR.getIsSuccess());
        assertTrue(avR.getData());

        // password
        DefUserPasswordUpdateVO pwdVO = new DefUserPasswordUpdateVO();
        when(defUserService.updatePassword(pwdVO)).thenReturn(true);
        R<Boolean> pwdR = controller.updatePassword(pwdVO);
        assertTrue(pwdR.getIsSuccess());
        assertTrue(pwdR.getData());

        // baseInfo
        DefUserBaseInfoUpdateVO baseInfoVO = new DefUserBaseInfoUpdateVO();
        when(defUserService.updateBaseInfo(baseInfoVO)).thenReturn(true);
        R<Boolean> baseR = controller.updateBaseInfo(baseInfoVO);
        assertTrue(baseR.getIsSuccess());
        assertTrue(baseR.getData());
    }

    @Test
    @DisplayName("测试 updateMobile 验证码校验各分支（成功、失败、null）")
    void testUpdateMobile() {
        OauthUserBiz oauthUserBiz = Mockito.mock(OauthUserBiz.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);

        UserInfoController controller = new UserInfoController(oauthUserBiz, userInfoService, defUserService, captchaService);

        DefUserMobileUpdateVO data = new DefUserMobileUpdateVO();
        data.setMobile("13900000000");
        data.setCode("1234");
        data.setTemplateCode("SMS_UPDATE");

        // 分支 1: 验证码校验为 null
        when(captchaService.checkCaptcha("13900000000", "SMS_UPDATE", "1234")).thenReturn(null);
        R<Boolean> rNull = controller.updateMobile(data);
        assertFalse(rNull.getIsSuccess());

        // 分支 2: 验证码校验失败
        when(captchaService.checkCaptcha("13900000000", "SMS_UPDATE", "1234")).thenReturn(R.fail("验证码错误"));
        R<Boolean> rFail = controller.updateMobile(data);
        assertFalse(rFail.getIsSuccess());

        // 分支 3: 验证码校验成功并更新
        when(captchaService.checkCaptcha("13900000000", "SMS_UPDATE", "1234")).thenReturn(R.success(true));
        when(defUserService.updateMobile(data)).thenReturn(true);
        R<Boolean> rSucc = controller.updateMobile(data);
        assertTrue(rSucc.getIsSuccess());
        assertTrue(rSucc.getData());
    }

    @Test
    @DisplayName("测试 updateEmail 验证码校验各分支（成功、失败、null）")
    void testUpdateEmail() {
        OauthUserBiz oauthUserBiz = Mockito.mock(OauthUserBiz.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);

        UserInfoController controller = new UserInfoController(oauthUserBiz, userInfoService, defUserService, captchaService);

        DefUserEmailUpdateVO data = new DefUserEmailUpdateVO();
        data.setEmail("test@email.com");
        data.setCode("1234");
        data.setTemplateCode("EMAIL_UPDATE");

        // 分支 1: 验证码校验为 null
        when(captchaService.checkCaptcha("test@email.com", "EMAIL_UPDATE", "1234")).thenReturn(null);
        R<Boolean> rNull = controller.updateEmail(data);
        assertFalse(rNull.getIsSuccess());

        // 分支 2: 验证码校验失败
        when(captchaService.checkCaptcha("test@email.com", "EMAIL_UPDATE", "1234")).thenReturn(R.fail("验证码错误"));
        R<Boolean> rFail = controller.updateEmail(data);
        assertFalse(rFail.getIsSuccess());

        // 分支 3: 验证码校验成功并更新
        when(captchaService.checkCaptcha("test@email.com", "EMAIL_UPDATE", "1234")).thenReturn(R.success(true));
        when(defUserService.updateEmail(data)).thenReturn(true);
        R<Boolean> rSucc = controller.updateEmail(data);
        assertTrue(rSucc.getIsSuccess());
        assertTrue(rSucc.getData());
    }

    @Test
    @DisplayName("测试 findCompanyDept 与 findDeptByCompany 组织架构接口")
    void testOrgQueries() {
        OauthUserBiz oauthUserBiz = Mockito.mock(OauthUserBiz.class);
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);

        UserInfoController controller = new UserInfoController(oauthUserBiz, userInfoService, defUserService, captchaService);

        OrgResultVO orgResult = OrgResultVO.builder().build();
        when(userInfoService.findCompanyAndDept()).thenReturn(orgResult);
        R<OrgResultVO> rOrg = controller.findCompanyDept();
        assertTrue(rOrg.getIsSuccess());
        assertNotNull(rOrg.getData());

        when(userInfoService.findDeptByCompany(10L, 20L)).thenReturn(Collections.singletonList(new BaseOrg()));
        R<List<BaseOrg>> rDept = controller.findDeptByCompany(10L, 20L);
        assertTrue(rDept.getIsSuccess());
        assertEquals(1, rDept.getData().size());
        verify(userInfoService).findDeptByCompany(10L, 20L);
    }
}
