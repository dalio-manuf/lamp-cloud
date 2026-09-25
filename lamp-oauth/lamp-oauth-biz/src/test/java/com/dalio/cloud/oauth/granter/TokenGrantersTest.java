package com.dalio.cloud.oauth.granter;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.model.enumeration.base.OrgTypeEnum;
import com.dalio.cloud.oauth.enumeration.GrantType;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.vo.param.LoginParamVO;
import com.dalio.cloud.oauth.vo.result.LoginResultVO;
import com.dalio.cloud.system.entity.system.DefClient;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.system.DefClientService;
import com.dalio.cloud.system.service.tenant.DefUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 授权类型 TokenGranter 单元测试
 */
class TokenGrantersTest {

    private SystemProperties systemProperties;
    private DefClientService defClientService;
    private DefUserService defUserService;
    private BaseEmployeeService baseEmployeeService;
    private BaseOrgService baseOrgService;
    private SaTokenConfig saTokenConfig;
    private CaptchaService captchaService;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringUtils.setApplicationContext(applicationContext);

        systemProperties = new SystemProperties();
        systemProperties.setVerifyPassword(true);
        systemProperties.setVerifyCaptcha(true);
        systemProperties.setMaxPasswordErrorNum(5);
        systemProperties.setPasswordErrorLockUserTime("1h");

        defClientService = Mockito.mock(DefClientService.class);
        defUserService = Mockito.mock(DefUserService.class);
        baseEmployeeService = Mockito.mock(BaseEmployeeService.class);
        baseOrgService = Mockito.mock(BaseOrgService.class);
        saTokenConfig = new SaTokenConfig();
        saTokenConfig.setTimeout(7200L);
        cn.dev33.satoken.SaManager.setConfig(saTokenConfig);
        cn.dev33.satoken.context.mock.SaTokenContextMockUtil.setMockContext();
        captchaService = Mockito.mock(CaptchaService.class);

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        cn.dev33.satoken.context.mock.SaTokenContextMockUtil.clearContext();
        ContextUtil.remove();
        try {
            StpUtil.logout();
        } catch (Exception ignored) {
        }
    }

    private void injectFields(AbstractTokenGranter granter) {
        granter.systemProperties = this.systemProperties;
        granter.defClientService = this.defClientService;
        granter.defUserService = this.defUserService;
        granter.baseEmployeeService = this.baseEmployeeService;
        granter.baseOrgService = this.baseOrgService;
        granter.saTokenConfig = this.saTokenConfig;
    }

    @Test
    @DisplayName("测试 PasswordTokenGranter 参数校验、用户查找、密码校验与成功登录流程")
    void testPasswordTokenGranterFlow() {
        PasswordTokenGranter granter = new PasswordTokenGranter();
        injectFields(granter);

        // 1. 参数校验失败
        LoginParamVO emptyParam = new LoginParamVO();
        R<LoginResultVO> checkParamRes = granter.checkParam(emptyParam);
        assertFalse(checkParamRes.getIsSuccess());

        // 2. 客户端未配置或错误
        LoginParamVO param = new LoginParamVO();
        param.setUsername("testuser");
        param.setPassword("123456");

        assertThrows(BizException.class, () -> granter.login(param));

        // 客户端未找到配置
        String wrongHeader = "Basic " + Base64.encode("wrong:wrong");
        request.addHeader(ContextConstants.CLIENT_KEY, wrongHeader);
        when(defClientService.getClient("wrong", "wrong")).thenReturn(null);
        R<LoginResultVO> loginNoClient = granter.login(param);
        assertFalse(loginNoClient.getIsSuccess());
        assertTrue(loginNoClient.getMsg().contains("配置正确的客户端ID"));

        // 客户端有效
        String authHeader = "Basic " + Base64.encode("clientId:clientSecret");
        request.removeHeader(ContextConstants.CLIENT_KEY);
        request.addHeader(ContextConstants.CLIENT_KEY, authHeader);
        DefClient client = new DefClient();
        client.setClientId("clientId");
        client.setState(true);
        when(defClientService.getClient("clientId", "clientSecret")).thenReturn(client);

        // 3. 用户不存在
        when(defUserService.getUserByUsername("testuser")).thenReturn(null);
        R<LoginResultVO> userNotFound = granter.login(param);
        assertFalse(userNotFound.getIsSuccess());

        // 4. 用户存在，密码错误
        DefUser user = new DefUser();
        user.setId(100L);
        user.setUsername("testuser");
        user.setNickName("测试用户");
        user.setSalt("salt123");
        user.setPassword(SecureUtil.sha256("correct_pass" + "salt123"));
        user.setState(true);
        user.setPasswordErrorNum(0);
        when(defUserService.getUserByUsername("testuser")).thenReturn(user);

        R<LoginResultVO> wrongPassRes = granter.login(param);
        assertFalse(wrongPassRes.getIsSuccess());
        assertTrue(wrongPassRes.getMsg().contains("密码错误"));

        // 5. 密码过期
        user.setPasswordExpireTime(LocalDateTime.now().minusDays(1));
        user.setPassword(SecureUtil.sha256("123456" + "salt123"));
        R<LoginResultVO> passExpiredRes = granter.login(param);
        assertFalse(passExpiredRes.getIsSuccess());
        assertTrue(passExpiredRes.getMsg().contains("已过期"));

        // 6. 密码连续错误达到限制并锁定
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(30));
        user.setPasswordErrorNum(5);
        user.setPasswordErrorLastTime(LocalDateTime.now());
        R<LoginResultVO> lockRes = granter.login(param);
        assertFalse(lockRes.getIsSuccess());
        assertTrue(lockRes.getMsg().contains("用户将被锁定"));

        // 7. 用户被禁用
        user.setPasswordErrorNum(0);
        user.setState(false);
        R<LoginResultVO> disabledUserRes = granter.login(param);
        assertFalse(disabledUserRes.getIsSuccess());
        assertTrue(disabledUserRes.getMsg().contains("您已被禁用"));

        // 8. 正常登录流程
        user.setState(true);
        BaseEmployeeResultVO emp = new BaseEmployeeResultVO();
        emp.setId(200L);
        emp.setState(true);
        when(baseEmployeeService.listEmployeeByUserId(100L)).thenReturn(Collections.singletonList(emp));

        BaseEmployee employeeEntity = new BaseEmployee();
        employeeEntity.setId(200L);
        employeeEntity.setLastCompanyId(300L);
        employeeEntity.setLastDeptId(400L);
        when(baseEmployeeService.getByIdCache(200L)).thenReturn(employeeEntity);

        BaseOrg company = new BaseOrg();
        company.setId(300L);
        company.setTreePath("/0/300/");
        when(baseOrgService.getByIdCache(300L)).thenReturn(company);

        R<LoginResultVO> successRes = granter.login(param);
        assertTrue(successRes.getIsSuccess());
        assertNotNull(successRes.getData());
        assertNotNull(successRes.getData().getToken());
    }

    @Test
    @DisplayName("测试 CaptchaTokenGranter 验证码校验逻辑")
    void testCaptchaTokenGranter() {
        CaptchaTokenGranter granter = new CaptchaTokenGranter(captchaService);
        injectFields(granter);

        LoginParamVO param = new LoginParamVO();
        param.setUsername("testuser");
        param.setPassword("123456");
        param.setCode("abcd");
        param.setKey("uuid-key");

        // 验证码错误抛出 BizException
        when(captchaService.checkCaptcha("uuid-key", CaptchaTokenGranter.GRANT_TYPE, "abcd"))
                .thenReturn(R.fail("验证码错误"));

        assertThrows(BizException.class, () -> granter.checkCaptcha(param));

        // 验证码正确
        when(captchaService.checkCaptcha("uuid-key", CaptchaTokenGranter.GRANT_TYPE, "abcd"))
                .thenReturn(R.success(true));

        R<LoginResultVO> okRes = granter.checkCaptcha(param);
        assertTrue(okRes.getIsSuccess());
    }

    @Test
    @DisplayName("测试 MobileTokenGranter 手机号登录校验与用户检索")
    void testMobileTokenGranter() {
        MobileTokenGranter granter = new MobileTokenGranter(captchaService);
        injectFields(granter);

        LoginParamVO param = new LoginParamVO();
        param.setMobile("13800138000");
        param.setCode("1234");

        // 短信验证码错误
        when(captchaService.checkCaptcha(eq("13800138000"), anyString(), eq("1234")))
                .thenReturn(R.fail("短信验证码错误"));
        assertThrows(BizException.class, () -> granter.checkCaptcha(param));

        // 短信验证码正确
        when(captchaService.checkCaptcha(eq("13800138000"), anyString(), eq("1234")))
                .thenReturn(R.success(true));
        assertTrue(granter.checkCaptcha(param).getIsSuccess());

        // 用户检索
        DefUser user = new DefUser();
        user.setId(500L);
        user.setMobile("13800138000");
        when(defUserService.getUserByMobile("13800138000")).thenReturn(user);
        assertEquals(user, granter.getUser(param));
    }

    @Test
    @DisplayName("测试 RefreshTokenGranter 刷新令牌有效与失效逻辑")
    void testRefreshTokenGranter() {
        RefreshTokenGranter granter = new RefreshTokenGranter(saTokenConfig);

        // 1. 无效/过期 token
        assertThrows(BizException.class, () -> granter.refresh("invalid-token-string"));

        // 2. 正常 token 刷新
        cn.hutool.json.JSONObject obj = new cn.hutool.json.JSONObject();
        obj.set(ContextConstants.JWT_KEY_USER_ID, 100L);
        obj.set(ContextConstants.JWT_KEY_TOP_COMPANY_ID, 300L);
        obj.set(ContextConstants.JWT_KEY_COMPANY_ID, 300L);
        obj.set(ContextConstants.JWT_KEY_DEPT_ID, 400L);
        obj.set(ContextConstants.JWT_KEY_EMPLOYEE_ID, 200L);
        String validRefreshToken = cn.dev33.satoken.temp.SaTempUtil.createToken(obj.toString(), 7200L);
        LoginResultVO resultVO = granter.refresh(validRefreshToken);
        assertNotNull(resultVO);
        assertNotNull(resultVO.getToken());
        assertNotNull(resultVO.getRefreshToken());
    }

    @Test
    @DisplayName("测试 TokenGranterBuilder 策略映射与降级异常")
    void testTokenGranterBuilder() {
        Map<String, TokenGranter> granterMap = new HashMap<>();
        PasswordTokenGranter passwordGranter = new PasswordTokenGranter();
        granterMap.put(GrantType.PASSWORD.name(), passwordGranter);

        TokenGranterBuilder builder = new TokenGranterBuilder(granterMap);

        // 正常获取
        assertSame(passwordGranter, builder.getGranter(GrantType.PASSWORD));
        assertSame(passwordGranter, builder.getGranter());

        // 未配置的策略抛异常
        assertThrows(BizException.class, () -> builder.getGranter(GrantType.CAPTCHA));
        assertThrows(BizException.class, () -> builder.getGranter(null));
    }

    @Test
    @DisplayName("测试 AbstractTokenGranter switchOrg 租户部门切换")
    void testSwitchOrg() {
        PasswordTokenGranter granter = new PasswordTokenGranter();
        injectFields(granter);

        // 模拟已登录用户
        StpUtil.login(1001L, "PC");
        ContextUtil.setUserId(1001L);

        DefUser user = new DefUser();
        user.setId(1001L);
        user.setState(true);
        when(defUserService.getByIdCache(1001L)).thenReturn(user);

        BaseEmployee employee = new BaseEmployee();
        employee.setId(2001L);
        employee.setState(true);
        when(baseEmployeeService.getEmployeeByUser(1001L)).thenReturn(employee);

        BaseOrg targetDept = new BaseOrg();
        targetDept.setId(3001L);
        targetDept.setType(OrgTypeEnum.DEPT.getCode());
        targetDept.setTreePath("/0/4001/3001/");
        when(baseOrgService.getByIdCache(3001L)).thenReturn(targetDept);

        BaseOrg company = new BaseOrg();
        company.setId(4001L);
        company.setType(OrgTypeEnum.COMPANY.getCode());
        company.setTreePath("/0/4001/");
        when(baseOrgService.getCompanyByDeptId(3001L)).thenReturn(company);
        when(baseOrgService.getByIdCache(4001L)).thenReturn(company);

        LoginResultVO resultVO = granter.switchOrg(3001L);
        assertNotNull(resultVO);
        assertNotNull(resultVO.getToken());

        // 测试登出
        R<Boolean> logoutRes = granter.logout();
        assertTrue(logoutRes.getIsSuccess());
    }

    @Test
    @DisplayName("测试 CaptchaTokenGranter 参数校验与验证码校验失败分支")
    void testCaptchaTokenGranterBranches() {
        CaptchaTokenGranter granter = new CaptchaTokenGranter(captchaService);
        injectFields(granter);

        // 1. checkParam 用户名或密码为空
        LoginParamVO p1 = new LoginParamVO();
        R<LoginResultVO> r1 = granter.checkParam(p1);
        assertFalse(r1.getIsSuccess());
        assertEquals("请输入用户名或密码", r1.getMsg());

        // 2. checkParam 验证码或key为空
        LoginParamVO p2 = new LoginParamVO();
        p2.setUsername("user");
        p2.setPassword("pwd");
        R<LoginResultVO> r2 = granter.checkParam(p2);
        assertFalse(r2.getIsSuccess());
        assertEquals("请输入验证码", r2.getMsg());

        // 3. checkCaptcha 当 verifyCaptcha 为 true 且校验失败
        systemProperties.setVerifyCaptcha(true);
        LoginParamVO p3 = new LoginParamVO();
        p3.setUsername("user");
        p3.setPassword("pwd");
        p3.setCode("0000");
        p3.setKey("k1");

        when(captchaService.checkCaptcha("k1", CaptchaTokenGranter.GRANT_TYPE, "0000")).thenReturn(null);
        assertThrows(BizException.class, () -> granter.checkCaptcha(p3));

        when(captchaService.checkCaptcha("k1", CaptchaTokenGranter.GRANT_TYPE, "0000")).thenReturn(R.fail("验证码已过期"));
        assertThrows(BizException.class, () -> granter.checkCaptcha(p3));

        // 校验成功
        when(captchaService.checkCaptcha("k1", CaptchaTokenGranter.GRANT_TYPE, "0000")).thenReturn(R.success(true));
        assertTrue(granter.checkCaptcha(p3).getIsSuccess());

        // verifyCaptcha 为 false 跳过
        systemProperties.setVerifyCaptcha(false);
        assertTrue(granter.checkCaptcha(p3).getIsSuccess());
    }

    @Test
    @DisplayName("测试 MobileTokenGranter 参数校验与验证码校验失败分支")
    void testMobileTokenGranterBranches() {
        MobileTokenGranter granter = new MobileTokenGranter(captchaService);
        injectFields(granter);

        // 1. checkParam 手机号或验证码为空
        LoginParamVO p1 = new LoginParamVO();
        R<LoginResultVO> r1 = granter.checkParam(p1);
        assertFalse(r1.getIsSuccess());
        assertEquals("请输入手机号或验证码", r1.getMsg());

        // 2. checkCaptcha 失败分支
        systemProperties.setVerifyCaptcha(true);
        LoginParamVO p2 = new LoginParamVO();
        p2.setMobile("13800000000");
        p2.setCode("0000");

        when(captchaService.checkCaptcha("13800000000", com.dalio.cloud.model.enumeration.base.MsgTemplateCodeEnum.MOBILE_LOGIN.getCode(), "0000"))
                .thenReturn(null);
        assertThrows(BizException.class, () -> granter.checkCaptcha(p2));

        when(captchaService.checkCaptcha("13800000000", com.dalio.cloud.model.enumeration.base.MsgTemplateCodeEnum.MOBILE_LOGIN.getCode(), "0000"))
                .thenReturn(R.fail("短信验证码失效"));
        assertThrows(BizException.class, () -> granter.checkCaptcha(p2));

        // 校验成功
        when(captchaService.checkCaptcha("13800000000", com.dalio.cloud.model.enumeration.base.MsgTemplateCodeEnum.MOBILE_LOGIN.getCode(), "0000"))
                .thenReturn(R.success(true));
        assertTrue(granter.checkCaptcha(p2).getIsSuccess());

        // verifyCaptcha 为 false 跳过
        systemProperties.setVerifyCaptcha(false);
        assertTrue(granter.checkCaptcha(p2).getIsSuccess());
    }

    @Test
    @DisplayName("测试 AbstractTokenGranter 状态校验与 switchOrg 异常分支")
    void testAbstractTokenGranterBranches() {
        PasswordTokenGranter granter = new PasswordTokenGranter();
        injectFields(granter);

        // checkUserState 为 null 或禁用
        R<LoginResultVO> userNullRes = granter.checkUserState(null);
        assertFalse(userNullRes.getIsSuccess());

        DefUser disabledUser = new DefUser();
        disabledUser.setId(999L);
        disabledUser.setState(false);
        R<LoginResultVO> userDisabledRes = granter.checkUserState(disabledUser);
        assertFalse(userDisabledRes.getIsSuccess());

        // switchOrg 用户为空抛出异常
        StpUtil.login(1002L, "PC");
        ContextUtil.setUserId(1002L);
        when(defUserService.getByIdCache(1002L)).thenReturn(null);
        assertThrows(com.dalio.basic.exception.UnauthorizedException.class, () -> granter.switchOrg(10L));

        // switchOrg 用户被禁用
        DefUser uDisabled = new DefUser();
        uDisabled.setId(1002L);
        uDisabled.setState(false);
        when(defUserService.getByIdCache(1002L)).thenReturn(uDisabled);
        assertThrows(com.dalio.basic.exception.UnauthorizedException.class, () -> granter.switchOrg(10L));

        // switchOrg 员工为空抛出异常
        DefUser u2 = new DefUser();
        u2.setId(1002L);
        u2.setState(true);
        when(defUserService.getByIdCache(1002L)).thenReturn(u2);
        when(baseEmployeeService.getEmployeeByUser(1002L)).thenReturn(null);
        assertThrows(com.dalio.basic.exception.ArgumentException.class, () -> granter.switchOrg(10L));

        // switchOrg 员工被禁用抛出异常
        BaseEmployee empDisabled = new BaseEmployee();
        empDisabled.setId(2002L);
        empDisabled.setState(false);
        when(baseEmployeeService.getEmployeeByUser(1002L)).thenReturn(empDisabled);
        assertThrows(BizException.class, () -> granter.switchOrg(10L));

        // switchOrg 组织为空抛出异常
        BaseEmployee emp = new BaseEmployee();
        emp.setId(2002L);
        emp.setState(true);
        when(baseEmployeeService.getEmployeeByUser(1002L)).thenReturn(emp);
        when(baseOrgService.getByIdCache(9999L)).thenReturn(null);
        assertThrows(com.dalio.basic.exception.ArgumentException.class, () -> granter.switchOrg(9999L));

        // switchOrg 目标组织为 COMPANY 类型
        BaseOrg targetCompany = new BaseOrg();
        targetCompany.setId(5001L);
        targetCompany.setType(OrgTypeEnum.COMPANY.getCode());
        targetCompany.setTreePath("/0/5001/");
        when(baseOrgService.getByIdCache(5001L)).thenReturn(targetCompany);
        LoginResultVO compSwitchRes = granter.switchOrg(5001L);
        assertNotNull(compSwitchRes);
        assertNotNull(compSwitchRes.getToken());
    }
}
