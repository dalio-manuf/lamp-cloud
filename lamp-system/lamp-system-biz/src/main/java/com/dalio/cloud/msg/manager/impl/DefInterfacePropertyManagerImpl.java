package com.dalio.cloud.msg.manager.impl;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import com.dalio.cloud.msg.manager.DefInterfacePropertyManager;
import com.dalio.cloud.msg.mapper.DefInterfacePropertyMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 通用业务实现类
 * 接口属性
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DefInterfacePropertyManagerImpl extends SuperManagerImpl<DefInterfacePropertyMapper, DefInterfaceProperty> implements DefInterfacePropertyManager {
    @Override
    public Map<String, Object> listByInterfaceId(Long id) {
        List<DefInterfaceProperty> propertyList = list(Wraps.<DefInterfaceProperty>lbQ().eq(DefInterfaceProperty::getInterfaceId, id));
        Map<String, Object> param = MapUtil.newHashMap();
        propertyList.forEach(item -> param.put(item.getKey(), item.getValue()));
        return param;
    }
}


