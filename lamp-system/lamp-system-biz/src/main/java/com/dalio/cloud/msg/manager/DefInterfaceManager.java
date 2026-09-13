package com.dalio.cloud.msg.manager;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.msg.entity.DefInterface;

/**
 * <p>
 * 通用业务接口
 * 接口
 * </p>
 *
 * @author admin
 * @date 2022-07-04 16:45:45
 * @create [2022-07-04 16:45:45] [admin] [代码生成器生成]
 */
public interface DefInterfaceManager extends SuperManager<DefInterface> {

    /**
     * 根据类型查询接口
     *
     * @param type
     * @return
     */
    DefInterface getByType(String type);
}


