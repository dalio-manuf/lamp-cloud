package com.dalio.cloud.msg.manager.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.manager.ExtendMsgRecipientManager;
import com.dalio.cloud.msg.mapper.ExtendMsgRecipientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 消息接收人
 * </p>
 *
 * @author admin
 * @date 2022-07-10 11:41:17
 * @create [2022-07-10 11:41:17] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExtendMsgRecipientManagerImpl extends SuperManagerImpl<ExtendMsgRecipientMapper, ExtendMsgRecipient> implements ExtendMsgRecipientManager {
    @Override
    public List<ExtendMsgRecipient> listByMsgId(Long id) {
        return list(Wraps.<ExtendMsgRecipient>lbQ().eq(ExtendMsgRecipient::getMsgId, id));
    }
}


