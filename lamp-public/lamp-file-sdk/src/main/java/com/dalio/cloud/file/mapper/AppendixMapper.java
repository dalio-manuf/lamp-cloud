package com.dalio.cloud.file.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.file.entity.Appendix;

/**
 * <p>
 * Mapper 接口
 * 业务附件
 * </p>
 *
 * @author admin
 * @date 2021-06-30
 * @create [2021-06-30] [admin] [初始创建]
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface AppendixMapper extends SuperMapper<Appendix> {

}
