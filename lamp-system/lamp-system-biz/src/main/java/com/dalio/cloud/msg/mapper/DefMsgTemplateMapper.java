package com.dalio.cloud.msg.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * 消息模板
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefMsgTemplateMapper extends SuperMapper<DefMsgTemplate> {

}


