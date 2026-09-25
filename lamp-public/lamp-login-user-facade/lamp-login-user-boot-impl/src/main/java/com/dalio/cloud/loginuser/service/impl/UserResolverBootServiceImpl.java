package com.dalio.cloud.loginuser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.dalio.basic.base.R;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.entity.user.BasePosition;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.service.user.BasePositionService;
import com.dalio.cloud.model.entity.base.SysEmployee;
import com.dalio.cloud.model.entity.base.SysOrg;
import com.dalio.cloud.model.entity.base.SysPosition;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.oauth.biz.ResourceBiz;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.tenant.DefUserService;
import com.dalio.cloud.userinfo.service.UserResolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户注入 boot 版本实现
 *
 * @author admin
 * @since 2024年09月20日14:15:47
 *
 */
@Component
@RequiredArgsConstructor
public class UserResolverBootServiceImpl implements UserResolverService {
    private final DefUserService defUserService;
    private final BaseEmployeeService baseEmployeeService;
    private final BaseRoleService baseRoleService;
    private final BaseOrgService baseOrgService;
    private final BasePositionService basePositionService;
    private final ResourceBiz resourceBiz;

    private boolean notEmpty(Long val) {
        return val != null && val != 0L;
    }

    @Override
    public R<SysUser> getById(UserQuery query) {
        Long userId = query.getUserId();
        DefUser defUser = defUserService.getByIdCache(userId);
        if (defUser == null) {
            return R.success(new SysUser());
        }
        SysUser sysUser = BeanUtil.toBean(defUser, SysUser.class);
        boolean notEmptyEmployee = notEmpty(query.getEmployeeId());
        boolean isFull = Boolean.TRUE.equals(query.getFull());
        boolean queryEmployee = isFull || Boolean.TRUE.equals(query.getEmployee());
        boolean queryOrg = isFull || Boolean.TRUE.equals(query.getOrg());
        boolean queryCurrentOrg = isFull || Boolean.TRUE.equals(query.getCurrentOrg());
        boolean queryPosition = isFull || Boolean.TRUE.equals(query.getPosition());
        boolean queryResource = isFull || Boolean.TRUE.equals(query.getResource());
        boolean queryRoles = isFull || Boolean.TRUE.equals(query.getRoles());
        boolean anyQuery = queryEmployee || queryOrg || queryCurrentOrg || queryPosition || queryResource || queryRoles;
        if (notEmptyEmployee && anyQuery) {
            BaseEmployee baseEmployee = baseEmployeeService.getByIdCache(query.getEmployeeId());
            if (baseEmployee == null) {
                return R.success(sysUser);
            }
            sysUser.setEmployee(BeanUtil.toBean(baseEmployee, SysEmployee.class));
            // 当前单位
            if (queryCurrentOrg && notEmpty(baseEmployee.getLastCompanyId())) {
                BaseOrg baseOrg = baseOrgService.getByIdCache(baseEmployee.getLastCompanyId());
                sysUser.setCompany(BeanUtil.toBean(baseOrg, SysOrg.class));
            }
            // 当前部门
            if (queryCurrentOrg && notEmpty(baseEmployee.getLastDeptId())) {
                BaseOrg baseOrg = baseOrgService.getByIdCache(baseEmployee.getLastDeptId());
                sysUser.setDept(BeanUtil.toBean(baseOrg, SysOrg.class));
            }
            // 他所在的 单位和部门
            if (queryOrg) {
                List<BaseOrg> companyList = baseOrgService.findCompanyByEmployeeId(baseEmployee.getId());
                sysUser.setCompanyList(BeanUtil.copyToList(companyList, SysOrg.class));

                List<BaseOrg> deptList = baseOrgService.findDeptByEmployeeId(baseEmployee.getId(), baseEmployee.getLastCompanyId());
                sysUser.setDeptList(BeanUtil.copyToList(deptList, SysOrg.class));
            }
            // 岗位
            if (queryPosition && notEmpty(baseEmployee.getPositionId())) {
                BasePosition basePosition = basePositionService.getById(baseEmployee.getPositionId());
                sysUser.setPosition(BeanUtil.toBean(basePosition, SysPosition.class));
            }
            // 资源
            if (queryResource) {
                List<String> resources = resourceBiz.findVisibleResource(baseEmployee.getId(), null);
                sysUser.setResourceCodeList(resources);
            }
            // 角色
            if (queryRoles) {
                List<String> codes = baseRoleService.findRoleCodeByEmployeeId(baseEmployee.getId());
                sysUser.setRoleCodeList(codes);
            }
        }
        return R.success(sysUser);
    }
}
