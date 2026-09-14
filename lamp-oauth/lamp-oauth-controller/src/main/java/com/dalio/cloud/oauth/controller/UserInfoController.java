package com.dalio.cloud.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.oauth.biz.OauthUserBiz;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.service.UserInfoService;
import com.dalio.cloud.oauth.vo.result.DefUserInfoResultVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.system.vo.update.tenant.DefUserAvatarUpdateVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserBaseInfoUpdateVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserEmailUpdateVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserMobileUpdateVO;
import com.dalio.cloud.system.vo.update.tenant.DefUserPasswordUpdateVO;

import java.util.List;

/**
 * 认证Controller
 *
 * @author admin
 * @date 2020年03月31日10:10:36
 */
@Slf4j
@RestController
@RequestMapping("/anyone")
@RequiredArgsConstructor
@Tag(name = "用户基本信息")
public class UserInfoController {

    private final OauthUserBiz oauthUserBiz;
    private final UserInfoService userInfoService;
    private final DefUserService defUserService;
    private final CaptchaService captchaService;

    /**
     * 获取当前登录的用户信息
     */
    @Operation(summary = "获取当前登录的用户信息", description = "获取当前登录的用户信息：登录后，查询用户信息")
    @GetMapping(value = "/getUserInfoById")
    public R<DefUserInfoResultVO> getUserInfoById(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = ContextUtil.getUserId();
        }
        return R.success(oauthUserBiz.getUserById(userId));
    }

    /**
     * 修改头像
     *
     * @param data 用户头像信息
     * @return 用户
     */
    @Operation(summary = "修改头像", description = "修改头像")
    @PutMapping("/avatar")
    @WebLog("'修改头像:' + #data.id")
    public R<Boolean> avatar(@RequestBody @Validated DefUserAvatarUpdateVO data) {
        return R.success(defUserService.updateAvatar(data));
    }

    /**
     * 修改密码
     *
     * @param data 修改实体
     * @return 是否成功
     */
    @Operation(summary = "修改密码", description = "修改密码")
    @PutMapping("/password")
    @WebLog("'修改密码:' + #data.id")
    public R<Boolean> updatePassword(@RequestBody @Validated DefUserPasswordUpdateVO data) {
        return R.success(defUserService.updatePassword(data));
    }

    /**
     * 修改手机
     *
     * @param data 修改实体
     * @return 是否成功
     */
    @Operation(summary = "修改手机", description = "修改手机")
    @PutMapping("/mobile")
    @WebLog("'修改手机:' + #data.mobile")
    public R<Boolean> updateMobile(@RequestBody @Validated DefUserMobileUpdateVO data) {
        R<Boolean> r = captchaService.checkCaptcha(data.getMobile(), data.getTemplateCode(), data.getCode());
        if (r == null || !r.getIsSuccess()) {
            return r != null ? r : R.fail("验证码校验失败");
        }
        return R.success(defUserService.updateMobile(data));
    }

    /**
     * 修改邮箱
     *
     * @param data 修改实体
     * @return 是否成功
     */
    @Operation(summary = "修改邮箱", description = "修改邮箱")
    @PutMapping("/email")
    @WebLog("'修改邮箱:' + #data.email")
    public R<Boolean> updateEmail(@RequestBody @Validated DefUserEmailUpdateVO data) {
        R<Boolean> r = captchaService.checkCaptcha(data.getEmail(), data.getTemplateCode(), data.getCode());
        if (r == null || !r.getIsSuccess()) {
            return r != null ? r : R.fail("验证码校验失败");
        }
        return R.success(defUserService.updateEmail(data));
    }

    /**
     * 修改个人信息
     *
     * @param data 用户基础信息
     * @return 用户
     */
    @Operation(summary = "修改基础信息")
    @PutMapping("/baseInfo")
    @WebLog(value = "'修改基础信息:' + #data?.id", request = false)
    public R<Boolean> updateBaseInfo(@RequestBody @Validated DefUserBaseInfoUpdateVO data) {
        return R.success(defUserService.updateBaseInfo(data));
    }


    @Operation(summary = "查询单位和部门")
    @GetMapping("/findCompanyDept")
    @WebLog(value = "根据租户ID查询单位和部门")
    public R<OrgResultVO> findCompanyDept() {
        return R.success(userInfoService.findCompanyAndDept());
    }

    @Operation(summary = "根据单位查询部门")
    @WebLog(value = "根据单位查询部门")
    @GetMapping("/findDeptByCompany")
    public R<List<BaseOrg>> findDeptByCompany(@RequestParam Long companyId, @RequestParam Long employeeId) {
        return R.success(userInfoService.findDeptByCompany(companyId, employeeId));
    }


}
