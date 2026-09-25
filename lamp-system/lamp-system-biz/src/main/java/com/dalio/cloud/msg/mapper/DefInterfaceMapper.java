package com.dalio.cloud.msg.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.msg.entity.DefInterface;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * 接口
 * </p>
 *
 * @author admin
 * @date 2022-07-04 16:45:45
 * @create [2022-07-04 16:45:45] [admin] [代码生成器生成]
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefInterfaceMapper extends SuperMapper<DefInterface> {

}


