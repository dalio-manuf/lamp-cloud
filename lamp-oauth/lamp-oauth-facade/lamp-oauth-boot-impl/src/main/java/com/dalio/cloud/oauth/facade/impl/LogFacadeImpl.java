package com.dalio.cloud.oauth.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dalio.basic.model.log.OptLogDTO;
import com.dalio.basic.utils.BeanPlusUtil;
import com.dalio.cloud.base.service.system.BaseOperationLogService;
import com.dalio.cloud.base.vo.save.system.BaseOperationLogSaveVO;
import com.dalio.cloud.oauth.facade.LogFacade;

/**
 * 操作日志保存 API
 *
 * @author admin
 * @date 2019/07/02
 */
@Service
@RequiredArgsConstructor
public class LogFacadeImpl implements LogFacade {
    private final BaseOperationLogService baseOperationLogService;

    /**
     * 保存日志
     *
     * @param data 操作日志
     * @return 操作日志
     */
    @Override
    public void save(OptLogDTO data) {
        BaseOperationLogSaveVO bean = BeanPlusUtil.toBean(data, BaseOperationLogSaveVO.class);
        baseOperationLogService.save(bean);
    }

}
