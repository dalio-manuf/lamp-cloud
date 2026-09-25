package com.dalio.cloud.msg.manager.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.manager.ExtendMsgManager;
import com.dalio.cloud.msg.mapper.ExtendMsgMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
 * 消息
 * </p>
 *
 * @author admin
 * @date 2022-07-10 11:41:17
 * @create [2022-07-10 11:41:17] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExtendMsgManagerImpl extends SuperManagerImpl<ExtendMsgMapper, ExtendMsg> implements ExtendMsgManager {

}


