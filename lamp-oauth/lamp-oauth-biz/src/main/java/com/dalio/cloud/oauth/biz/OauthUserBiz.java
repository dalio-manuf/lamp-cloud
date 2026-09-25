package com.dalio.cloud.oauth.biz;

import cn.hutool.core.bean.BeanUtil;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;
import com.dalio.cloud.common.constant.AppendixType;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.model.vo.result.AppendixResultVO;
import com.dalio.cloud.oauth.vo.result.DefUserInfoResultVO;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.application.DefApplicationService;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户大业务
 *
 * @author admin
 * @date 2021/10/28 13:09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthUserBiz {
    private final BaseEmployeeService baseEmployeeService;
    private final DefUserService defUserService;
    private final DefApplicationService defApplicationService;
    private final AppendixService appendixService;

    public DefUserInfoResultVO getUserById(Long id) {
        // 查默认库
        DefUser defUser = defUserService.getByIdCache(id);
        if (defUser == null) {
            return null;
        }

        // 用户信息
        DefUserInfoResultVO resultVO = new DefUserInfoResultVO();
        BeanUtil.copyProperties(defUser, resultVO);

        // 用户头像
        AppendixResultVO appendix = appendixService.getByBiz(defUser.getId(), AppendixType.System.DEF__USER__AVATAR);
        if (appendix != null) {
            resultVO.setAvatarId(appendix.getId());
        }

        Long employeeId = ContextUtil.getEmployeeId();
        resultVO.setEmployeeId(employeeId);

        //查 租户库
        if (employeeId != null && employeeId > 0) {
            BaseEmployee employee = baseEmployeeService.getByIdCache(employeeId);
            resultVO.setBaseEmployee(BeanUtil.toBean(employee, BaseEmployeeResultVO.class));
        }

        DefApplication defApplication = defApplicationService.getDefApp(id);
        resultVO.setDefApplication(BeanUtil.toBean(defApplication, DefApplicationResultVO.class));
        return resultVO;
    }
}
