package com.dalio.cloud.oauth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.common.cache.auth.TempAdminCacheKeyBuilder;
import com.dalio.cloud.common.cache.common.CaptchaCacheKeyBuilder;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.oauth.service.UserInfoService;
import com.dalio.cloud.oauth.vo.param.RegisterByEmailVO;
import com.dalio.cloud.oauth.vo.param.RegisterByMobileVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.tenant.DefUserService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @version v1.0
 * @date 2022/9/16 12:21 PM
 * @create [2022/9/16 12:21 PM ] [admin] [初始创建]
 */
@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final BaseEmployeeService baseEmployeeService;
    private final BaseOrgService baseOrgService;
    private final DefUserService defUserService;
    private final CacheOps cacheOps;
    private final SystemProperties systemProperties;

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return "00:00:00";
        }
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    @Override
    public OrgResultVO findCompanyAndDept() {
        Long userId = ContextUtil.getUserId();
        Long companyId = ContextUtil.getCurrentCompanyId();
        Long deptId = ContextUtil.getCurrentDeptId();
        BaseEmployee baseEmployee = baseEmployeeService.getEmployeeByUser(userId);
        ArgumentAssert.notNull(baseEmployee, "用户不属于该企业");

        // 上次登录的单位
        List<BaseOrg> orgList = baseOrgService.findOrgByEmployeeId(baseEmployee.getId());

        Long currentCompanyId = companyId != null ? companyId : baseEmployee.getLastCompanyId();

        Long currentDeptId = deptId != null ? deptId : baseEmployee.getLastDeptId();
        return OrgResultVO.builder()
                .orgList(orgList)
                .employeeId(baseEmployee.getId())
                .currentCompanyId(currentCompanyId)
                .currentDeptId(currentDeptId).build();
    }

    @Override
    public List<BaseOrg> findDeptByCompany(Long companyId, Long employeeId) {
        return baseOrgService.findDeptByEmployeeId(employeeId, companyId);
    }

    @Override
    public String registerByMobile(RegisterByMobileVO register) {
        ArgumentAssert.equals(register.getConfirmPassword(), register.getPassword(), "密码和确认密码不一致");
        if (Boolean.TRUE.equals(systemProperties.getVerifyCaptcha())) {
//            短信验证码
            CacheKey cacheKey = new CaptchaCacheKeyBuilder().key(register.getMobile(), register.getKey());
            CacheResult<String> code = cacheOps.get(cacheKey);
            ArgumentAssert.equals(code != null ? code.getValue() : null, register.getCode(), "验证码不正确");
            cacheOps.del(cacheKey);
        }
        DefUser defUser = BeanUtil.toBean(register, DefUser.class);

        defUserService.register(defUser);

        return defUser.getMobile();
    }

    @Override
    public String registerByEmail(RegisterByEmailVO register) {
        ArgumentAssert.equals(register.getConfirmPassword(), register.getPassword(), "密码和确认密码不一致");
        if (Boolean.TRUE.equals(systemProperties.getVerifyCaptcha())) {
//            短信验证码
            CacheKey cacheKey = new CaptchaCacheKeyBuilder().key(register.getEmail(), register.getKey());
            CacheResult<String> code = cacheOps.get(cacheKey);
            ArgumentAssert.equals(code != null ? code.getValue() : null, register.getCode(), "验证码不正确");
            cacheOps.del(cacheKey);
        }
        DefUser defUser = BeanUtil.toBean(register, DefUser.class);

        defUserService.registerByEmail(defUser);

        return defUser.getEmail();
    }

    @Override
    public Map<String, Object> registerTempAdmin(String type) {
        Map<String, Object> result = new HashMap<>(4);
        String username = RandomUtil.randomNumbers(4);
        String password = RandomUtil.randomNumbers(2);
        CacheKey key = TempAdminCacheKeyBuilder.builder(username);
        CacheKey typeKey = TempAdminCacheKeyBuilder.builder(username, "type");
        cacheOps.set(key, username + password);
        cacheOps.set(typeKey, type);

        result.put("username", username);
        result.put("password", username + password);
        result.put("expire", key.getExpire());
        result.put("expireStr", formatDuration(key.getExpire()));
        return result;
    }
}
