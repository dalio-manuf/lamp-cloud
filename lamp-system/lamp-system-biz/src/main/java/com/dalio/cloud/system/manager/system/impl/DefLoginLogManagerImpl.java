package com.dalio.cloud.system.manager.system.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.manager.system.DefLoginLogManager;
import com.dalio.cloud.system.mapper.system.DefLoginLogMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 登录日志
 * </p>
 *
 * @author admin
 * @date 2021-11-12
 * @create [2021-11-12] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefLoginLogManagerImpl extends SuperManagerImpl<DefLoginLogMapper, DefLoginLog> implements DefLoginLogManager {
    @Override
    public Long clearLog(LocalDateTime clearBeforeTime, Integer clearBeforeNum) {
        List<Long> idList = Collections.emptyList();
        if (clearBeforeNum != null) {
            Page<DefLoginLog> page = super.page(new Page<>(0, clearBeforeNum), Wraps.<DefLoginLog>lbQ().select(DefLoginLog::getId).orderByDesc(DefLoginLog::getCreatedTime));
            idList = page.getRecords().stream().map(DefLoginLog::getId).toList();
        }
        return baseMapper.clearLog(clearBeforeTime, idList);
    }
}
