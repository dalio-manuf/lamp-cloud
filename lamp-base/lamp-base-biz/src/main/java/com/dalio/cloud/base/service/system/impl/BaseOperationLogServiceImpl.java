package com.dalio.cloud.base.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.cloud.base.entity.system.BaseOperationLog;
import com.dalio.cloud.base.entity.system.BaseOperationLogExt;
import com.dalio.cloud.base.manager.system.BaseOperationLogManager;
import com.dalio.cloud.base.mapper.system.BaseOperationLogExtMapper;
import com.dalio.cloud.base.service.system.BaseOperationLogService;
import com.dalio.cloud.base.vo.result.system.BaseOperationLogResultVO;
import com.dalio.cloud.base.vo.save.system.BaseOperationLogSaveVO;

import java.time.LocalDateTime;

/**
 * <p>
 * 业务实现类
 * 操作日志
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseOperationLogServiceImpl extends SuperServiceImpl<BaseOperationLogManager, Long, BaseOperationLog> implements BaseOperationLogService {

    private final BaseOperationLogExtMapper baseOperationLogExtMapper;

    @Override
    public BaseOperationLogResultVO getDetail(Long id) {
        BaseOperationLog operationLog = superManager.getById(id);
        BaseOperationLogExt ext = baseOperationLogExtMapper.selectById(id);

        BaseOperationLogResultVO result = BeanUtil.toBean(ext, BaseOperationLogResultVO.class);
        BeanUtil.copyProperties(operationLog, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearLog(LocalDateTime clearBeforeTime, Integer clearBeforeNum) {
        return superManager.clearLog(clearBeforeTime, clearBeforeNum) > 0;
    }

    @Override
    public <SaveVO> BaseOperationLog save(SaveVO saveVO) {
        BaseOperationLogSaveVO logSaveVO = (BaseOperationLogSaveVO) saveVO;
        BaseOperationLogExt baseOperationLogExt = BeanUtil.toBean(saveVO, BaseOperationLogExt.class);
        baseOperationLogExtMapper.insert(baseOperationLogExt);
        logSaveVO.setId(baseOperationLogExt.getId());
        return super.save(logSaveVO);
    }
}
