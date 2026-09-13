package com.dalio.cloud.base.mapper;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import com.dalio.basic.annotation.database.TenantLine;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.base.entity.user.BaseEmployee;


/**
 * 仅仅测试使用
 *
 * @author admin
 * @date 2021-10-18
 */
@Repository
@TenantLine
public interface BaseEmployeeTestMapper extends SuperMapper<BaseEmployee> {
    /**
     * get
     *
     * @param id id
     * @return com.dalio.cloud.base.entity.user.BaseEmployee
     * @author admin
     * @date 2022/10/28 4:38 PM
     */
    @TenantLine(false)
    @Select("select * from base_employee where id = #{id}")
    BaseEmployee get(Long id);

}
