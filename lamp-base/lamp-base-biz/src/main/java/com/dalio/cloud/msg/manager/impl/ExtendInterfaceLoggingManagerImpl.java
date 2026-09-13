package com.dalio.cloud.msg.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.msg.entity.ExtendInterfaceLogging;
import com.dalio.cloud.msg.manager.ExtendInterfaceLoggingManager;
import com.dalio.cloud.msg.mapper.ExtendInterfaceLoggingMapper;

/**
 * <p>
 * 通用业务实现类
 * 接口执行日志记录
 * </p>
 *
 * @author admin
 * @date 2022-07-09 23:58:59
 * @create [2022-07-09 23:58:59] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExtendInterfaceLoggingManagerImpl extends SuperManagerImpl<ExtendInterfaceLoggingMapper, ExtendInterfaceLogging> implements ExtendInterfaceLoggingManager {

}


