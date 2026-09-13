package com.dalio.cloud.oauth.biz;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.common.constant.RoleConstant;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.service.application.DefResourceService;

import java.util.Collections;
import java.util.List;

import static com.dalio.basic.context.ContextConstants.JWT_KEY_EMPLOYEE_ID;

/**
 * sa-token 权限实现
 *
 * @author admin
 * @since 2024/7/26 17:35
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StpInterfaceBiz {
    private final DefResourceService defResourceService;
    private final BaseRoleService baseRoleService;

    public List<String> getPermissionList() {
        SaSession tokenSession = StpUtil.getTokenSession();
        long employeeId = tokenSession.getLong(JWT_KEY_EMPLOYEE_ID);
        // 超管 返回 *

        List<DefResource> list;
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        List<String> resourceCodes = Collections.emptyList();
        if (isAdmin) {
            // 管理员 拥有所有权限
            list = Collections.singletonList(DefResource.builder().code("*").build());
        } else {
            List<Long> resourceIdList = baseRoleService.findResourceIdByEmployeeId(null, employeeId);
            if (resourceIdList.isEmpty()) {
                return Collections.emptyList();
            }

            list = defResourceService.findByIdsAndType(resourceIdList, resourceCodes);
        }
        return CollHelper.split(list, DefResource::getCode, StrPool.SEMICOLON);
    }

    public List<String> getRoleList() {
        SaSession tokenSession = StpUtil.getTokenSession();
        long employeeId = tokenSession.getLong(JWT_KEY_EMPLOYEE_ID);
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        if (isAdmin) {
            return List.of("*");
        } else {
            return baseRoleService.findRoleCodeByEmployeeId(employeeId);
        }
    }
}
