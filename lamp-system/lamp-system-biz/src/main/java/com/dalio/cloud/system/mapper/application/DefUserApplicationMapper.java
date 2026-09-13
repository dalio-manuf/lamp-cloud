package com.dalio.cloud.system.mapper.application;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.application.DefUserApplication;

/**
 * <p>
 * Mapper 接口
 * 用户的默认应用
 * </p>
 *
 * @author admin
 * @date 2022-03-06
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefUserApplicationMapper extends SuperMapper<DefUserApplication> {

}
