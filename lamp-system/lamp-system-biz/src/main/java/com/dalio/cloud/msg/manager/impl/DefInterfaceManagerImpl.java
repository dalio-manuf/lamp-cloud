package com.dalio.cloud.msg.manager.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.msg.entity.DefInterface;
import com.dalio.cloud.msg.manager.DefInterfaceManager;
import com.dalio.cloud.msg.mapper.DefInterfaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
 * 接口
 * </p>
 *
 * @author admin
 * @date 2022-07-04 16:45:45
 * @create [2022-07-04 16:45:45] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DefInterfaceManagerImpl extends SuperManagerImpl<DefInterfaceMapper, DefInterface> implements DefInterfaceManager {
    @Override
    public DefInterface getByType(String type) {
        return getOne(Wraps.<DefInterface>lbQ().eq(DefInterface::getCode, type));
    }
}


