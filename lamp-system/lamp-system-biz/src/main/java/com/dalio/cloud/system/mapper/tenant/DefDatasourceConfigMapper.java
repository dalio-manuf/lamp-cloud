package com.dalio.cloud.system.mapper.tenant;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.tenant.DefDatasourceConfig;

/**
 * <p>
 * Mapper 接口
 * 数据源
 * </p>
 *
 * @author admin
 * @date 2021-09-13
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefDatasourceConfigMapper extends SuperMapper<DefDatasourceConfig> {

}
