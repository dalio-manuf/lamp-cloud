package com.dalio.cloud.msg.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * 接口属性
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefInterfacePropertyMapper extends SuperMapper<DefInterfaceProperty> {

}


