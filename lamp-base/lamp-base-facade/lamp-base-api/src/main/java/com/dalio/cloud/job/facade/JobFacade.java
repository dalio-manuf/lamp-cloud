package com.dalio.cloud.job.facade;

import com.dalio.basic.base.R;
import com.dalio.cloud.job.dto.XxlJobInfoVO;

/**
 * @author admin
 * @since 2024年09月21日00:15:26
 */
public interface JobFacade {
    /**
     * 定时发送接口
     *
     * @param xxlJobInfo 任务
     * @return 任务id
     */
    R<String> addTimingTask(XxlJobInfoVO xxlJobInfo);

}
