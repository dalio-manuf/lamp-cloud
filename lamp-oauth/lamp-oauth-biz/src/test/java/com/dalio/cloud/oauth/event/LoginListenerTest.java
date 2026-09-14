package com.dalio.cloud.oauth.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.cloud.oauth.event.listener.LoginListener;
import com.dalio.cloud.oauth.event.model.LoginStatusDTO;
import com.dalio.cloud.system.enumeration.system.LoginStatusEnum;
import com.dalio.cloud.system.service.system.DefLoginLogService;
import com.dalio.cloud.system.service.tenant.DefUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * 登录事件监听测试
 */
class LoginListenerTest {

    @Test
    @DisplayName("测试 LoginListener 登录成功重置错误次数与密码错误自增")
    void testSaveSysLog() {
        DefLoginLogService loginLogService = Mockito.mock(DefLoginLogService.class);
        DefUserService userService = Mockito.mock(DefUserService.class);
        LoginListener listener = new LoginListener(loginLogService, userService);

        // 1. 登录成功
        LoginStatusDTO successDTO = LoginStatusDTO.success(100L, 200L);
        listener.saveSysLog(new LoginEvent(successDTO));
        verify(userService).resetPassErrorNum(100L);
        verify(loginLogService).save(any());

        // 2. 密码错误
        LoginStatusDTO errorDTO = LoginStatusDTO.fail(100L, LoginStatusEnum.PASSWORD_ERROR, "密码错误");
        listener.saveSysLog(new LoginEvent(errorDTO));
        verify(userService).incrPasswordErrorNumById(100L);

        // 3. 用户不存在错误 (userId 为空)
        LoginStatusDTO noUserDTO = LoginStatusDTO.fail("nonexistent", LoginStatusEnum.USER_ERROR, "用户不存在");
        listener.saveSysLog(new LoginEvent(noUserDTO));
    }
}
