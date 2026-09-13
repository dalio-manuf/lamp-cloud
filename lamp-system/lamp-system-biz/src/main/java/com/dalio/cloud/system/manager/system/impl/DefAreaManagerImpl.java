package com.dalio.cloud.system.manager.system.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.system.entity.system.DefArea;
import com.dalio.cloud.system.manager.system.DefAreaManager;
import com.dalio.cloud.system.mapper.system.DefAreaMapper;

/**
 * <p>
 * 通用业务实现类
 * 地区表
 * </p>
 *
 * @author admin
 * @date 2021-10-13
 * @create [2021-10-13] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefAreaManagerImpl extends SuperManagerImpl<DefAreaMapper, DefArea> implements DefAreaManager {
}
