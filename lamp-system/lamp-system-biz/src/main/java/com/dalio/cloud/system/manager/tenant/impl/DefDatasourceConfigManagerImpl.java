package com.dalio.cloud.system.manager.tenant.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.system.entity.tenant.DefDatasourceConfig;
import com.dalio.cloud.system.manager.tenant.DefDatasourceConfigManager;
import com.dalio.cloud.system.mapper.tenant.DefDatasourceConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 应用管理
 *
 * @author admin
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [admin] [初始创建]
 */
@RequiredArgsConstructor
@Service
public class DefDatasourceConfigManagerImpl extends SuperManagerImpl<DefDatasourceConfigMapper, DefDatasourceConfig> implements DefDatasourceConfigManager {
    @Override
    public DefDatasourceConfig getByName(String dsName) {
        return getOne(Wraps.<DefDatasourceConfig>lbQ().eq(DefDatasourceConfig::getName, dsName), false);
    }
}
