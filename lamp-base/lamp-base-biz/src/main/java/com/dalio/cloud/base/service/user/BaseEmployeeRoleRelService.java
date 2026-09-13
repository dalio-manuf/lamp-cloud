package com.dalio.cloud.base.service.user;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 员工的角色
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
public interface BaseEmployeeRoleRelService extends SuperService<Long, BaseEmployeeRoleRel> {
    /**
     * 给员工绑定指定的角色
     *
     * @param employeeIdList 员工
     * @param code           角色编码
     * @return
     */
    boolean bindRole(List<Long> employeeIdList, String code);

    /**
     * 解绑指定角色
     *
     * @param employeeIdList
     * @param code
     * @return
     */
    boolean unBindRole(List<Long> employeeIdList, String code);
}
