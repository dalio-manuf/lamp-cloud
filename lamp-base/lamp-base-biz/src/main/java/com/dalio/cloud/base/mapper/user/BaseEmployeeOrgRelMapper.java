package com.dalio.cloud.base.mapper.user;

import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 员工所在部门
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Repository
public interface BaseEmployeeOrgRelMapper extends SuperMapper<BaseEmployeeOrgRel> {

    /**
     * 查询员工拥有的机构
     *
     * @param employeeId employeeId
     * @return java.util.List<java.lang.Long>
     * @author admin
     * @date 2022/10/20 3:44 PM
     * @create [2022/10/20 3:44 PM ] [admin] [初始创建]
     */
    List<Long> selectOrgByEmployeeId(@Param("employeeId") Long employeeId);
}
