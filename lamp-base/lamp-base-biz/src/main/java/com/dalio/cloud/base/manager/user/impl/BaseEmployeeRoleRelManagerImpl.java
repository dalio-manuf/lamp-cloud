package com.dalio.cloud.base.manager.user.impl;

import cn.hutool.core.collection.CollUtil;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.system.BaseRoleManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.mapper.user.BaseEmployeeRoleRelMapper;
import com.dalio.cloud.common.cache.base.user.EmployeeRoleCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 员工的角色
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 * @create [2021-10-18] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseEmployeeRoleRelManagerImpl extends SuperManagerImpl<BaseEmployeeRoleRelMapper, BaseEmployeeRoleRel> implements BaseEmployeeRoleRelManager {
    private final BaseRoleManager baseRoleManager;
    private final CacheOps cacheOps;

    @Override
    public boolean removeByEmployeeIds(Collection<Long> employeeIds) {
        ArgumentAssert.notEmpty(employeeIds, "员工ID不能为空");
        boolean remove = remove(Wraps.<BaseEmployeeRoleRel>lbQ().in(BaseEmployeeRoleRel::getEmployeeId, employeeIds));
        cacheOps.del(employeeIds.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return remove;
    }

    @Override
    public boolean bindRole(List<Long> employeeIdList, String code) {
        BaseRole role = baseRoleManager.getRoleByCode(code);
        ArgumentAssert.notNull(role, "请先配置{}管理员", code);
        List<BaseEmployeeRoleRel> erList = employeeIdList.stream().map(employeeId -> {
            BaseEmployeeRoleRel employeeRole = new BaseEmployeeRoleRel();
            employeeRole.setEmployeeId(employeeId);
            employeeRole.setRoleId(role.getId());
            return employeeRole;
        }).toList();

        boolean flag = saveBatch(erList);

        cacheOps.del(employeeIdList.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return flag;
    }

    @Override
    public boolean unBindRole(List<Long> employeeIdList, String code) {
        ArgumentAssert.notEmpty(employeeIdList, "请传递员工");
        BaseRole role = baseRoleManager.getRoleByCode(code);
        ArgumentAssert.notNull(role, "请先配置{}管理员", code);
        boolean flag = remove(Wraps.<BaseEmployeeRoleRel>lbQ().eq(BaseEmployeeRoleRel::getRoleId, role.getId())
                .in(BaseEmployeeRoleRel::getEmployeeId, employeeIdList));
        cacheOps.del(employeeIdList.stream().map(EmployeeRoleCacheKeyBuilder::build).toList());
        return flag;
    }

    @Override
    public void deleteByRole(Collection<Long> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        LbQueryWrap<BaseEmployeeRoleRel> wrap = Wraps.<BaseEmployeeRoleRel>lbQ().in(BaseEmployeeRoleRel::getRoleId, roleIdList);
        List<BaseEmployeeRoleRel> list = list(wrap);
        remove(wrap);
        cacheOps.del(list.stream().map(BaseEmployeeRoleRel::getEmployeeId).distinct().map(EmployeeRoleCacheKeyBuilder::build).toList());
    }
}
