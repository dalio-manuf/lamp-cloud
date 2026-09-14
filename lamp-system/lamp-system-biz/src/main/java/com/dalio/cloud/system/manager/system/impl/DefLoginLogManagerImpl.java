package com.dalio.cloud.system.manager.system.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.manager.system.DefLoginLogManager;
import com.dalio.cloud.system.mapper.system.DefLoginLogMapper;

import java.time.LocalDateTime;

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
        Long cutoffId = null;
        if (clearBeforeNum != null && clearBeforeNum > 0) {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<DefLoginLog> page =
                    super.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(clearBeforeNum, 1, false),
                            Wraps.<DefLoginLog>lbQ().select(DefLoginLog::getId).orderByDesc(DefLoginLog::getId));
            if (page.getRecords().isEmpty()) {
                // 现有日志总数未超过保留数量，无需清理
                return 0L;
            }
            cutoffId = page.getRecords().get(0).getId();
        }
        if (clearBeforeTime == null && cutoffId == null) {
            // 安全防御：避免无条件全表误删
            return 0L;
        }
        return baseMapper.clearLog(clearBeforeTime, cutoffId, null);
    }
}
