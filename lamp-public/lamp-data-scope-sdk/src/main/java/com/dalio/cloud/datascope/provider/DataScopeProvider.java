package com.dalio.cloud.datascope.provider;

import com.dalio.cloud.datascope.model.DataFieldProperty;

import java.util.List;

/**
 * @author admin
 * @date 2022/1/9 23:28
 */
public interface DataScopeProvider {
    /**
     * 查询数据字段
     *
     * @param fsp fsp
     * @return java.util.List<com.dalio.cloud.datascope.model.DataFieldProperty>
     * @author admin
     * @date 2022/10/28 4:41 PM
     * @create [2022/10/28 4:41 PM ] [admin] [初始创建]
     */
    List<DataFieldProperty> findDataFieldProperty(List<DataFieldProperty> fsp);
}
