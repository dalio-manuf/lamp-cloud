package com.dalio.cloud.oauth.service;

import com.dalio.basic.base.R;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.model.enumeration.base.MsgTemplateCodeEnum;
import com.dalio.cloud.msg.facade.MsgFacade;
import com.dalio.cloud.oauth.properties.CaptchaProperties;
import com.dalio.cloud.oauth.service.impl.CaptchaServiceImpl;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.tenant.DefUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 验证码服务单元测试
 */
class CaptchaServiceTest {

    private CacheOps cacheOps;
    private CaptchaProperties captchaProperties;
    private MsgFacade msgFacade;
    private DefUserService defUserService;
    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() {
        cacheOps = Mockito.mock(CacheOps.class);
        captchaProperties = new CaptchaProperties();
        msgFacade = Mockito.mock(MsgFacade.class);
        defUserService = Mockito.mock(DefUserService.class);
        captchaService = new CaptchaServiceImpl(cacheOps, captchaProperties, msgFacade, defUserService);
    }

    @Test
    @DisplayName("测试 createImg 生成验证码图片与空白参数防御")
    void testCreateImg() throws IOException {
        // 空白 key 抛异常
        assertThrows(BizException.class, () -> captchaService.createImg("", new MockHttpServletResponse()));

        // 正常输出图片
        for (CaptchaProperties.CaptchaType type : CaptchaProperties.CaptchaType.values()) {
            captchaProperties.setType(type);
            MockHttpServletResponse response = new MockHttpServletResponse();
            captchaService.createImg("test-uuid-key", response);
            assertEquals(type.getContentType(), response.getContentType());
            assertTrue(response.getContentAsByteArray().length > 0);
        }
        verify(cacheOps, atLeastOnce()).set(any(CacheKey.class), anyString());
    }

    @Test
    @DisplayName("测试 sendSmsCode 手机验证码发送及重复/未注册状态防御")
    void testSendSmsCode() {
        String mobile = "13812345678";

        // 1. 注册验证码：已注册时抛异常
        when(defUserService.checkMobile(mobile, null)).thenReturn(true);
        assertThrows(Exception.class, () -> captchaService.sendSmsCode(mobile, MsgTemplateCodeEnum.REGISTER_SMS.getCode()));

        // 注册验证码：未注册时发送成功
        when(defUserService.checkMobile(mobile, null)).thenReturn(false);
        R<Boolean> regResult = captchaService.sendSmsCode(mobile, MsgTemplateCodeEnum.REGISTER_SMS.getCode());
        assertTrue(regResult.getIsSuccess());

        // 2. 登录验证码：未注册时抛异常
        assertThrows(Exception.class, () -> captchaService.sendSmsCode(mobile, MsgTemplateCodeEnum.MOBILE_LOGIN.getCode()));

        // 登录验证码：已注册时发送成功
        when(defUserService.checkMobile(mobile, null)).thenReturn(true);
        R<Boolean> loginResult = captchaService.sendSmsCode(mobile, MsgTemplateCodeEnum.MOBILE_LOGIN.getCode());
        assertTrue(loginResult.getIsSuccess());

        // 3. 修改手机号：已被占用时抛异常
        assertThrows(Exception.class, () -> captchaService.sendSmsCode(mobile, MsgTemplateCodeEnum.MOBILE_EDIT.getCode()));
    }

    @Test
    @DisplayName("测试 sendEmailCode 邮箱验证码发送")
    void testSendEmailCode() {
        String email = "test@example.com";

        // 注册邮箱：已被占用
        when(defUserService.checkEmail(email, null)).thenReturn(true);
        assertThrows(Exception.class, () -> captchaService.sendEmailCode(email, MsgTemplateCodeEnum.REGISTER_EMAIL.getCode()));

        // 注册邮箱：可用
        when(defUserService.checkEmail(email, null)).thenReturn(false);
        R<Boolean> result = captchaService.sendEmailCode(email, MsgTemplateCodeEnum.REGISTER_EMAIL.getCode());
        assertTrue(result.getIsSuccess());
    }

    @Test
    @DisplayName("测试 sendCodeByForgetPassword 忘记密码验证码发送及用户不存在检测")
    void testSendCodeByForgetPassword() {
        // 用户不存在
        when(defUserService.getUserByUsername("unknown")).thenReturn(null);
        assertThrows(Exception.class, () -> captchaService.sendCodeByForgetPassword("13800000000", "unknown"));

        // 手机号与用户名不匹配
        DefUser user = new DefUser();
        user.setUsername("alice");
        user.setMobile("13999999999");
        when(defUserService.getUserByUsername("alice")).thenReturn(user);
        assertThrows(Exception.class, () -> captchaService.sendCodeByForgetPassword("13800000000", "alice"));

        // 手机号与用户名匹配
        R<Boolean> result = captchaService.sendCodeByForgetPassword("13999999999", "alice");
        assertTrue(result.getIsSuccess());
    }

    @Test
    @DisplayName("测试 checkCaptcha 验证码正确、错误与过期校验")
    void testCheckCaptcha() {
        // 1. 空参数
        R<Boolean> emptyRes = captchaService.checkCaptcha("key1", "REGISTER", "");
        assertFalse(emptyRes.getIsSuccess());

        // 2. 验证码过期
        when(cacheOps.get(any(CacheKey.class))).thenReturn(null);
        R<Boolean> expiredRes = captchaService.checkCaptcha("key1", "REGISTER", "1234");
        assertFalse(expiredRes.getIsSuccess());
        assertTrue(expiredRes.getMsg().contains("过期"));

        // 3. 验证码错误
        @SuppressWarnings("unchecked")
        CacheResult<String> cachedWrong = Mockito.mock(CacheResult.class);
        when(cachedWrong.getValue()).thenReturn("abcd");
        doReturn(cachedWrong).when(cacheOps).get(any(CacheKey.class));

        R<Boolean> wrongRes = captchaService.checkCaptcha("key1", "REGISTER", "9999");
        assertFalse(wrongRes.getIsSuccess());
        assertTrue(wrongRes.getMsg().contains("不正确"));

        // 4. 验证码正确且清除缓存
        @SuppressWarnings("unchecked")
        CacheResult<String> cachedCorrect = Mockito.mock(CacheResult.class);
        when(cachedCorrect.getValue()).thenReturn("ABCD");
        doReturn(cachedCorrect).when(cacheOps).get(any(CacheKey.class));

        R<Boolean> okRes = captchaService.checkCaptcha("key1", "REGISTER", "abcd");
        assertTrue(okRes.getIsSuccess());
        verify(cacheOps).del(any(CacheKey.class));
    }
}
