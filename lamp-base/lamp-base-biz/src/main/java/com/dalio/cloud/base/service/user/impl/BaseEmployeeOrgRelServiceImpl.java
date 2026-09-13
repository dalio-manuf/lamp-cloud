package com.dalio.cloud.base.service.user.impl;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.service.user.BaseEmployeeOrgRelService;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 员工所在部门
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseEmployeeOrgRelServiceImpl extends SuperServiceImpl<BaseEmployeeOrgRelManager, Long, BaseEmployeeOrgRel> implements BaseEmployeeOrgRelService {
    @Override
    public List<Long> findOrgIdListByEmployeeId(Long employeeId) {
        ArgumentAssert.notNull(employeeId, "员工id不能为空");
        return superManager.listObjs(Wraps.<BaseEmployeeOrgRel>lbQ().select(BaseEmployeeOrgRel::getOrgId).eq(BaseEmployeeOrgRel::getEmployeeId, employeeId), Convert::toLong);
    }
}
