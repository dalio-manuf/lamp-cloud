package com.dalio.cloud.base.service.user.impl;


import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.service.user.BaseEmployeeRoleRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 员工的角色
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseEmployeeRoleRelServiceImpl extends SuperServiceImpl<BaseEmployeeRoleRelManager, Long, BaseEmployeeRoleRel> implements BaseEmployeeRoleRelService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindRole(List<Long> employeeIdList, String code) {
        return superManager.bindRole(employeeIdList, code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unBindRole(List<Long> employeeIdList, String code) {
        return superManager.unBindRole(employeeIdList, code);
    }
}
