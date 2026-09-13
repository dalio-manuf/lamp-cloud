package com.dalio.cloud.base.service.user;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 员工所在部门
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
public interface BaseEmployeeOrgRelService extends SuperService<Long, BaseEmployeeOrgRel> {
    /**
     * 根据员工id查询员工的机构id
     *
     * @param employeeId 员工id
     * @return
     */
    List<Long> findOrgIdListByEmployeeId(Long employeeId);
}
