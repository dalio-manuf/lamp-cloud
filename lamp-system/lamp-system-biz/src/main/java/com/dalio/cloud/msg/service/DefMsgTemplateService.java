package com.dalio.cloud.msg.service;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.msg.entity.DefMsgTemplate;


/**
 * <p>
 * 业务接口
 * 消息模板
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
public interface DefMsgTemplateService extends SuperService<Long, DefMsgTemplate> {
    /**
     * 检测 模板标识 是否存在
     *
     * @param code
     * @param id
     * @return
     */
    Boolean check(String code, Long id);

    /**
     * 根据消息模板编码查询消息模板
     *
     * @param code
     * @return
     */
    DefMsgTemplate getByCode(String code);
}


