package com.dalio.cloud.system.mapper.system;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.system.DefDict;

/**
 * <p>
 * Mapper 接口
 * 字典
 * </p>
 *
 * @author admin
 * @date 2021-10-04
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefDictMapper extends SuperMapper<DefDict> {

}
