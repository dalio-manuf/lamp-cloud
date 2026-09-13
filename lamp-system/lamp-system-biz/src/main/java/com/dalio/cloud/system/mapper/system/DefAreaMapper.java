package com.dalio.cloud.system.mapper.system;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.system.DefArea;

/**
 * <p>
 * Mapper 接口
 * 地区表
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefAreaMapper extends SuperMapper<DefArea> {

}
