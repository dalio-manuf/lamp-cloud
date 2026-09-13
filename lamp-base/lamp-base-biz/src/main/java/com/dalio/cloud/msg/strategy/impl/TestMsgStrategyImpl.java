package com.dalio.cloud.msg.strategy.impl;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.dalio.cloud.msg.strategy.MsgStrategy;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;

/**
 * @author admin
 * @date 2022/7/11 0011 10:29
 */
public class TestMsgStrategyImpl implements MsgStrategy {
    private static final Logger log = LoggerFactory.getLogger(TestMsgStrategyImpl.class);

    @Resource
    private ExtendMsgService extendMsgService;

    @Override
    public MsgResult exec(MsgParam msgParam) {

        ExtendMsg a = extendMsgService.getById(msgParam.getExtendMsg().getId());
        log.info("a {}", a);

        return MsgResult.builder().result("保存成功").build();
    }
}
