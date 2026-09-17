package com.dalio.cloud.job.facade.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.base.R;
import com.dalio.cloud.job.dto.XxlJobInfoVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * 单体版 JobFacadeImpl 测试
 *
 * @author went
 */
class JobFacadeImplTest {

    @Test
    @DisplayName("测试 addTimingTask 成功添加定时任务")
    void testAddTimingTask() {
        JobFacadeImpl jobFacade = new JobFacadeImpl();
        ReflectionTestUtils.setField(jobFacade, "jobServerUrl", "http://127.0.0.1:8767");

        XxlJobInfoVO jobInfo = new XxlJobInfoVO();
        jobInfo.setJobGroupName("group1");
        jobInfo.setJobDesc("desc1");

        HttpRequest mockRequest = mock(HttpRequest.class);
        HttpResponse mockResponse = mock(HttpResponse.class);

        when(mockRequest.body(anyString())).thenReturn(mockRequest);
        when(mockRequest.timeout(anyInt())).thenReturn(mockRequest);
        when(mockRequest.execute()).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("task-999");

        try (MockedStatic<HttpRequest> mockedHttpRequest = mockStatic(HttpRequest.class)) {
            mockedHttpRequest.when(() -> HttpRequest.post(anyString())).thenReturn(mockRequest);

            R<String> result = jobFacade.addTimingTask(jobInfo);

            assertNotNull(result);
            assertTrue(result.getIsSuccess());
            assertEquals("task-999", result.getData());
        }
    }
}
