package com.dalio.cloud.system.mapper.application;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.entity.application.DefResourceApi;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 资源接口
 * </p>
 *
 * @author admin
 * @date 2021-09-17
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefResourceApiMapper extends SuperMapper<DefResourceApi> {

    /**
     * 查询系统中配置的所有API与资源编码
     */
    @Select("""
            select ra.uri ,ra.request_method , r.code from def_resource_api ra inner join def_resource r on r.id = ra.resource_id 
            where r.state  = 1 order by r.sort_value asc
            """)
    List<ResourceApiVO> findAllApi();
}
