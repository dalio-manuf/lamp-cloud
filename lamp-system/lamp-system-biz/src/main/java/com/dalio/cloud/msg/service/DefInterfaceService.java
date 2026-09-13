package com.dalio.cloud.msg.service;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.msg.entity.DefInterface;


/**
 * <p>
 * 业务接口
 * 接口
 * </p>
 *
 * @author admin
 * @date 2022-07-04 16:45:45
 * @create [2022-07-04 16:45:45] [admin] [代码生成器生成]
 */
public interface DefInterfaceService extends SuperService<Long, DefInterface> {
    /**
     * 检查接口编码是否重复
     *
     * @param code
     * @param id
     * @return
     */
    Boolean check(String code, Long id);
}


