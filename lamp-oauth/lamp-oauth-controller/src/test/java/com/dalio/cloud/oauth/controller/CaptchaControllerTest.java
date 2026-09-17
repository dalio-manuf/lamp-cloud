package com.dalio.cloud.oauth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.service.CaptchaService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CaptchaController 单元测试
 */
class CaptchaControllerTest {

    @Test
    @DisplayName("测试 checkCaptcha 验证码校验")
    void testCheckCaptcha() {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(captchaService);

        when(captchaService.checkCaptcha("key1", "CAPTCHA", "1234")).thenReturn(R.success(true));

        R<Boolean> result = controller.checkCaptcha("key1", "1234", "CAPTCHA");
        assertTrue(result.getIsSuccess());
        assertTrue(result.getData());
        verify(captchaService).checkCaptcha("key1", "CAPTCHA", "1234");
    }

    @Test
    @DisplayName("测试 captcha 图片验证码生成")
    void testCaptchaImage() throws IOException {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(captchaService);
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.captcha("uuid-key", response);
        verify(captchaService).createImg("uuid-key", response);
    }

    @Test
    @DisplayName("测试 sendSmsCode 发送短信验证码")
    void testSendSmsCode() {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(captchaService);

        when(captchaService.sendSmsCode("13800000000", "SMS_TPL")).thenReturn(R.success(true));

        R<Boolean> result = controller.sendSmsCode("13800000000", "SMS_TPL");
        assertTrue(result.getIsSuccess());
        assertTrue(result.getData());
        verify(captchaService).sendSmsCode("13800000000", "SMS_TPL");
    }

    @Test
    @DisplayName("测试 sendEmailCode 发送邮箱验证码")
    void testSendEmailCode() {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(captchaService);

        when(captchaService.sendEmailCode("test@mail.com", "EMAIL_TPL")).thenReturn(R.success(true));

        R<Boolean> result = controller.sendEmailCode("test@mail.com", "EMAIL_TPL");
        assertTrue(result.getIsSuccess());
        assertEquals(true, result.getData());
        verify(captchaService).sendEmailCode("test@mail.com", "EMAIL_TPL");
    }

    @Test
    @DisplayName("测试 sendCodeByForgetPassword 忘记密码发送验证码")
    void testSendCodeByForgetPassword() {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(captchaService);

        when(captchaService.sendCodeByForgetPassword("13800000000", "admin")).thenReturn(R.success(true));

        R<Boolean> result = controller.sendCodeByForgetPassword("13800000000", "admin");
        assertTrue(result.getIsSuccess());
        assertTrue(result.getData());
        verify(captchaService).sendCodeByForgetPassword("13800000000", "admin");
    }
}
