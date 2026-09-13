package com.dalio.cloud.job.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.R;
import com.dalio.cloud.job.api.JobApi;
import com.dalio.cloud.job.dto.XxlJobInfoVO;
import com.dalio.cloud.job.facade.JobFacade;

/**
 *
 * @author admin
 * @since 2024/9/21 00:15
 */
@Service
@RequiredArgsConstructor
public class JobFacadeImpl implements JobFacade {
    @Autowired
    @Lazy  // 一定要延迟加载，否则lamp-gateway-server无法启动
    private JobApi jobApi;

    @Override
    public R<String> addTimingTask(XxlJobInfoVO xxlJobInfo) {
        return jobApi.addTimingTask(xxlJobInfo);
    }
}
