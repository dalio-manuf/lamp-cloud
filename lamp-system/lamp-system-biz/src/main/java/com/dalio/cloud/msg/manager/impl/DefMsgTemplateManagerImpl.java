package com.dalio.cloud.msg.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.manager.DefMsgTemplateManager;
import com.dalio.cloud.msg.mapper.DefMsgTemplateMapper;

/**
 * <p>
 * 通用业务实现类
 * 消息模板
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DefMsgTemplateManagerImpl extends SuperManagerImpl<DefMsgTemplateMapper, DefMsgTemplate> implements DefMsgTemplateManager {
    @Override
    public DefMsgTemplate getByCode(String code) {
        return getOne(Wraps.<DefMsgTemplate>lbQ().eq(DefMsgTemplate::getCode, code));
    }
}


