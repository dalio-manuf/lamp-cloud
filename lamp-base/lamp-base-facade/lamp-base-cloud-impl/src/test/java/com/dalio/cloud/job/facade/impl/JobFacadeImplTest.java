package com.dalio.cloud.job.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.job.api.JobApi;
import com.dalio.cloud.job.dto.XxlJobInfoVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微服务版 JobFacadeImpl 测试
 *
 * @author went
 */
@ExtendWith(MockitoExtension.class)
class JobFacadeImplTest {

    @Mock
    private JobApi jobApi;

    @InjectMocks
    private JobFacadeImpl jobFacade;

    @Test
    @DisplayName("测试微服务版 addTimingTask 转发到 JobApi")
    void testAddTimingTask() {
        XxlJobInfoVO vo = new XxlJobInfoVO();
        vo.setJobGroupName("group1");
        R<String> expected = R.success("task-123");

        when(jobApi.addTimingTask(vo)).thenReturn(expected);

        R<String> result = jobFacade.addTimingTask(vo);

        assertNotNull(result);
        assertEquals("task-123", result.getData());
        verify(jobApi).addTimingTask(vo);
    }
}
