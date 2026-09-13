package com.dalio.cloud.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.generator.entity.DefGenTable;

/**
 * <p>
 * Mapper 接口
 * 代码生成
 * </p>
 *
 * @author admin
 * @date 2022-03-01
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefGenTableMapper extends SuperMapper<DefGenTable> {

}
