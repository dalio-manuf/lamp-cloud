package com.dalio.cloud.system.mapper.system;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.system.DefParameter;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * 参数配置
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefParameterMapper extends SuperMapper<DefParameter> {

}
