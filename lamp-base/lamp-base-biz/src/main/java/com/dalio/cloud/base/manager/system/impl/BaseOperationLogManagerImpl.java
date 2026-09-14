package com.dalio.cloud.base.manager.system.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.cloud.base.entity.system.BaseOperationLog;
import com.dalio.cloud.base.manager.system.BaseOperationLogManager;
import com.dalio.cloud.base.mapper.system.BaseOperationLogExtMapper;
import com.dalio.cloud.base.mapper.system.BaseOperationLogMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 操作日志
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 * @create [2021-11-08] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseOperationLogManagerImpl extends SuperManagerImpl<BaseOperationLogMapper, BaseOperationLog> implements BaseOperationLogManager {
    private final BaseOperationLogExtMapper baseOperationLogExtMapper;

    @Override
    public Long clearLog(LocalDateTime clearBeforeTime, Integer clearBeforeNum) {
        if (clearBeforeTime == null && (clearBeforeNum == null || clearBeforeNum <= 0)) {
            // 安全防御：避免无条件全表误删
            return 0L;
        }
        List<Long> idList = Collections.emptyList();
        if (clearBeforeNum != null && clearBeforeNum > 0) {
            Page<BaseOperationLog> page = super.page(new Page<>(1, clearBeforeNum), Wraps.<BaseOperationLog>lbQ().select(BaseOperationLog::getId).orderByDesc(BaseOperationLog::getCreatedTime));
            idList = page.getRecords().stream().map(BaseOperationLog::getId).toList();
            if (clearBeforeTime == null && idList.isEmpty()) {
                return 0L;
            }
        }
        if (clearBeforeTime == null && idList.isEmpty()) {
            return 0L;
        }
        baseOperationLogExtMapper.clearLog(clearBeforeTime, idList);
        return baseMapper.clearLog(clearBeforeTime, idList);
    }

}
