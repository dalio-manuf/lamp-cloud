package com.dalio.cloud.msg.manager;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.msg.entity.DefMsgTemplate;

/**
 * <p>
 * 通用业务接口
 * 消息模板
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
public interface DefMsgTemplateManager extends SuperManager<DefMsgTemplate> {
    /**
     * 根据消息模板编码查询消息模板
     *
     * @param code
     * @return
     */
    DefMsgTemplate getByCode(String code);
}


