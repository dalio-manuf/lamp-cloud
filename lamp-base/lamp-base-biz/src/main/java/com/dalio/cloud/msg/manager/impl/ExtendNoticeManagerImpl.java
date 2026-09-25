package com.dalio.cloud.msg.manager.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.msg.entity.ExtendNotice;
import com.dalio.cloud.msg.manager.ExtendNoticeManager;
import com.dalio.cloud.msg.mapper.ExtendNoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
 * 通知表
 * </p>
 *
 * @author admin
 * @date 2022-07-04 15:51:37
 * @create [2022-07-04 15:51:37] [admin] [代码生成器生成]
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExtendNoticeManagerImpl extends SuperManagerImpl<ExtendNoticeMapper, ExtendNotice> implements ExtendNoticeManager {

}


