package com.dalio.cloud.base.manager.user;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 通用业务接口
 * 员工所在部门
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
public interface BaseEmployeeOrgRelManager extends SuperManager<BaseEmployeeOrgRel> {
    /**
     * 查询员工所属的机构
     *
     * @param employeeId employeeId
     * @return java.util.List<java.lang.Long>
     * @author admin
     * @date 2022/10/26 10:40 PM
     * @create [2022/10/26 10:40 PM ] [admin] [初始创建]
     */
    List<Long> findOrgIdByEmployeeId(Long employeeId);

    /**
     * 根据员工id删除 员工的组织机构
     *
     * @param employeeIds 员工
     * @return
     */
    boolean removeByEmployeeIds(Collection<Long> employeeIds);

    /**
     * 根据员工id删除 员工的组织机构
     *
     * @param employeeId 员工
     * @return
     */
    default boolean removeByEmployeeId(Long employeeId) {
        return removeByEmployeeIds(Collections.singletonList(employeeId));
    }

    /**
     * 根据机构ID删除机构下的员工
     *
     * @param orgIdList 机构ID
     * @author admin
     * @date 2022/10/25 9:22 PM
     * @create [2022/10/25 9:22 PM ] [admin] [初始创建]
     */
    void deleteByOrg(Collection<Long> orgIdList);
}
