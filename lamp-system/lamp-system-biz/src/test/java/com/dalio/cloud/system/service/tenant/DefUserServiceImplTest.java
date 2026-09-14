package com.dalio.cloud.system.service.tenant;

import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.base.R;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.manager.tenant.DefUserManager;
import com.dalio.cloud.system.service.tenant.impl.DefUserServiceImpl;
import com.dalio.cloud.system.vo.query.tenant.ForgetPasswordDto;
import com.dalio.cloud.system.vo.update.tenant.DefUserPasswordResetVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserPasswordUpdateVO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务实现类单元测试
 */
class DefUserServiceImplTest {

    @org.junit.jupiter.api.BeforeAll
    static void init() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                DefUser.class
        );
    }

    @Test
    @DisplayName("测试 checkUsername/Email/Mobile/IdCard 唯一性校验")
    void testUniquenessChecks() {
        DefUserManager userManager = Mockito.mock(DefUserManager.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);
        SystemProperties properties = new SystemProperties();

        DefUserServiceImpl service = new DefUserServiceImpl(appendixService, properties);
        ReflectionTestUtils.setField(service, "superManager", userManager);

        when(userManager.count(any())).thenReturn(1L);
        assertTrue(service.checkUsername("alice", 1L));
        assertTrue(service.checkEmail("alice@test.com", 1L));
        assertTrue(service.checkMobile("13800000000", 1L));
        assertTrue(service.checkIdCard("110101199001011234", 1L));

        when(userManager.count(any())).thenReturn(0L);
        assertFalse(service.checkUsername("bob", 2L));
        assertFalse(service.checkEmail("bob@test.com", 2L));
        assertFalse(service.checkMobile("13900000000", 2L));
        assertFalse(service.checkIdCard("110101199001015678", 2L));
    }

    @Test
    @DisplayName("测试 resetPassErrorNum 与 incrPasswordErrorNumById 输错密码计数管理")
    void testPasswordErrorCounters() {
        DefUserManager userManager = Mockito.mock(DefUserManager.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);
        SystemProperties properties = new SystemProperties();

        DefUserServiceImpl service = new DefUserServiceImpl(appendixService, properties);
        ReflectionTestUtils.setField(service, "superManager", userManager);

        // 1. 重置错误次数
        service.resetPassErrorNum(100L);
        verify(userManager).resetPassErrorNum(100L);

        // 2. 自增错误次数
        service.incrPasswordErrorNumById(100L);
        verify(userManager).incrPasswordErrorNumById(100L);
    }

    @Test
    @DisplayName("测试 resetPassword 重置密码与校验")
    void testResetPassword() {
        DefUserManager userManager = Mockito.mock(DefUserManager.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);
        SystemProperties properties = new SystemProperties();

        DefUserServiceImpl service = Mockito.spy(new DefUserServiceImpl(appendixService, properties));
        ReflectionTestUtils.setField(service, "superManager", userManager);

        DefUserPasswordResetVO resetVO = new DefUserPasswordResetVO();
        resetVO.setId(100L);
        resetVO.setIsUseSystemPassword(false);
        resetVO.setPassword("newPass123");
        resetVO.setConfirmPassword("mismatch");

        // 两次输入不一致
        assertThrows(Exception.class, () -> service.resetPassword(resetVO));

        // 正常重置
        resetVO.setConfirmPassword("newPass123");
        DefUser mockUser = new DefUser();
        mockUser.setId(100L);
        mockUser.setSalt("saltXYZ");
        when(userManager.getById(100L)).thenReturn(mockUser);
        when(userManager.update(any())).thenReturn(true);

        Boolean ok = service.resetPassword(resetVO);
        assertTrue(ok);
        verify(userManager).update(any());
    }

    @Test
    @DisplayName("测试 updatePassword 修改密码原密码校验与更新")
    void testUpdatePassword() {
        DefUserManager userManager = Mockito.mock(DefUserManager.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);
        SystemProperties properties = new SystemProperties();

        DefUserServiceImpl service = Mockito.spy(new DefUserServiceImpl(appendixService, properties));
        ReflectionTestUtils.setField(service, "superManager", userManager);

        DefUser mockUser = new DefUser();
        mockUser.setId(100L);
        mockUser.setSalt("salt1");
        mockUser.setPassword(SecureUtil.sha256("oldPass" + "salt1"));
        when(userManager.getById(100L)).thenReturn(mockUser);
        when(userManager.update(any())).thenReturn(true);

        com.dalio.basic.context.ContextUtil.setUserId(100L);
        try {
            DefUserPasswordUpdateVO updateVO = new DefUserPasswordUpdateVO();
            updateVO.setId(100L);
            updateVO.setOldPassword("wrongOldPass");
            updateVO.setPassword("newPass456");
            updateVO.setConfirmPassword("newPass456");

            // 旧密码错误
            assertThrows(ArgumentException.class, () -> service.updatePassword(updateVO));

            // 密码修改成功
            updateVO.setOldPassword("oldPass");
            Boolean result = service.updatePassword(updateVO);
            assertTrue(result);
            verify(userManager).update(any());
        } finally {
            com.dalio.basic.context.ContextUtil.setUserId(null);
        }
    }

    @Test
    @DisplayName("测试 forgetPassword 忘记密码找回与验证码校验")
    void testForgetPassword() {
        DefUserManager userManager = Mockito.mock(DefUserManager.class);
        AppendixService appendixService = Mockito.mock(AppendixService.class);
        SystemProperties properties = new SystemProperties();
        properties.setVerifyCaptcha(true);

        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        DefUserServiceImpl service = Mockito.spy(new DefUserServiceImpl(appendixService, properties));
        ReflectionTestUtils.setField(service, "superManager", userManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        DefUser user = new DefUser();
        user.setId(200L);
        user.setUsername("testuser");
        user.setMobile("13800138000");
        user.setSalt("salt2");
        when(userManager.getUserByUsername("testuser")).thenReturn(user);
        when(userManager.update(any())).thenReturn(true);

        ForgetPasswordDto dto = new ForgetPasswordDto();
        dto.setUsername("testuser");
        dto.setMobile("13800138000");
        dto.setCode("8888");
        dto.setPassword("resetPass");

        // 验证码错误
        when(cacheOps.get(any(CacheKey.class))).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.forgetPassword(dto));

        // 验证码匹配
        @SuppressWarnings("unchecked")
        CacheResult<String> codeResult = Mockito.mock(CacheResult.class);
        when(codeResult.getValue()).thenReturn("8888");
        doReturn(codeResult).when(cacheOps).get(any(CacheKey.class));

        R<Boolean> success = service.forgetPassword(dto);
        assertTrue(success.getIsSuccess());
        verify(cacheOps).del(any(CacheKey.class));
    }
}
