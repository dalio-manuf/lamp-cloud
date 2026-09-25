package com.dalio.cloud.system.manager.application.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.system.entity.application.DefUserApplication;
import com.dalio.cloud.system.manager.application.DefUserApplicationManager;
import com.dalio.cloud.system.mapper.application.DefUserApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
 * 用户的默认应用
 * </p>
 *
 * @author admin
 * @date 2022-03-06
 * @create [2022-03-06] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefUserApplicationManagerImpl extends SuperManagerImpl<DefUserApplicationMapper, DefUserApplication> implements DefUserApplicationManager {
}
