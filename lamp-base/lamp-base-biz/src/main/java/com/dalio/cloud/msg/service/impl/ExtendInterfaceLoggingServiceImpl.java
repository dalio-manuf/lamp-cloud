package com.dalio.cloud.msg.service.impl;


import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.cloud.msg.entity.ExtendInterfaceLogging;
import com.dalio.cloud.msg.manager.ExtendInterfaceLoggingManager;
import com.dalio.cloud.msg.service.ExtendInterfaceLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
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
@Transactional(readOnly = true)
public class ExtendInterfaceLoggingServiceImpl extends SuperServiceImpl<ExtendInterfaceLoggingManager, Long, ExtendInterfaceLogging> implements ExtendInterfaceLoggingService {


}


