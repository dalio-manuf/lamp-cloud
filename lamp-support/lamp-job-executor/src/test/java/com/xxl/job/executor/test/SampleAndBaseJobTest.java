package com.xxl.job.executor.test;

import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.executor.NoneExecutorServerApplication;
import com.xxl.job.executor.core.config.XxlJobConfig;
import com.xxl.job.executor.service.jobhandler.BaseJob;
import com.xxl.job.executor.service.jobhandler.SampleXxlJob;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.service.ExtendMsgService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SampleAndBaseJobTest {

    @Mock
    private MsgBiz msgBiz;

    @Mock
    private ExtendMsgService extendMsgService;

    @Test
    void testXxlJobConfig() {
        XxlJobConfig config = new XxlJobConfig();
        ReflectionTestUtils.setField(config, "adminAddresses", "http://127.0.0.1:8080/xxl-job-admin");
        ReflectionTestUtils.setField(config, "accessToken", "default_token");
        ReflectionTestUtils.setField(config, "appname", "lamp-executor");
        ReflectionTestUtils.setField(config, "address", "");
        ReflectionTestUtils.setField(config, "ip", "127.0.0.1");
        ReflectionTestUtils.setField(config, "port", 9999);
        ReflectionTestUtils.setField(config, "logPath", "/data/applogs/xxl-job/jobhandler");
        ReflectionTestUtils.setField(config, "logRetentionDays", 30);

        XxlJobSpringExecutor executor = config.xxlJobExecutor();
        assertNotNull(executor);
    }

    @Test
    void testSampleXxlJobLifecycleAndDemo() throws Exception {
        SampleXxlJob job = new SampleXxlJob();
        job.init();
        job.destroy();

        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            job.demoJobHandler2();
            mocked.verify(() -> XxlJobHelper.log("XXL-JOB, Hello World."));
        }
    }

    @Test
    void testSampleXxlJobSharding() throws Exception {
        SampleXxlJob job = new SampleXxlJob();
        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            mocked.when(XxlJobHelper::getShardIndex).thenReturn(0);
            mocked.when(XxlJobHelper::getShardTotal).thenReturn(2);

            job.shardingJobHandler();

            mocked.verify(() -> XxlJobHelper.log("第 {} 片, 命中分片开始处理", 0));
            mocked.verify(() -> XxlJobHelper.log("第 {} 片, 忽略", 1));
        }
    }

    @Test
    void testSampleXxlJobCommand() throws Exception {
        SampleXxlJob job = new SampleXxlJob();
        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            mocked.when(XxlJobHelper::getJobParam).thenReturn("/bin/echo");
            job.commandJobHandler();
            mocked.verify(() -> XxlJobHelper.handleFail(anyString()), never());
        }

        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            mocked.when(XxlJobHelper::getJobParam).thenReturn("nonexistent_cmd_12345");
            job.commandJobHandler();
            mocked.verify(() -> XxlJobHelper.handleFail(anyString()), atLeastOnce());
        }
    }

    @Test
    void testSampleXxlJobHttp() throws Exception {
        SampleXxlJob job = new SampleXxlJob();
        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            // Null or empty param
            mocked.when(XxlJobHelper::getJobParam).thenReturn(null);
            job.httpJobHandler();
            mocked.verify(XxlJobHelper::handleFail, times(1));

            // Missing url
            mocked.when(XxlJobHelper::getJobParam).thenReturn("method: GET\n");
            job.httpJobHandler();
            mocked.verify(XxlJobHelper::handleFail, times(2));

            // Invalid method
            mocked.when(XxlJobHelper::getJobParam).thenReturn("url: http://localhost:8080\nmethod: DELETE\n");
            job.httpJobHandler();
            mocked.verify(XxlJobHelper::handleFail, times(3));

            // Valid params but target port unavailable (catches Exception)
            mocked.when(XxlJobHelper::getJobParam).thenReturn("url: http://127.0.0.1:54321/test\nmethod: POST\ndata: {\"test\":1}\n");
            job.httpJobHandler();
            mocked.verify(XxlJobHelper::handleFail, times(4));
        }
    }

    @Test
    void testBaseJobSendMsg() {
        BaseJob baseJob = new BaseJob(msgBiz, extendMsgService);

        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            // null param
            mocked.when(XxlJobHelper::getJobParam).thenReturn(null);
            assertThrows(Exception.class, baseJob::sendMsg);

            // empty map
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{}");
            baseJob.sendMsg();

            // invalid msgId
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"msgId\": \"abc\"}");
            baseJob.sendMsg();
            mocked.verify(() -> XxlJobHelper.handleFail(contains("msgId 参数无效")));

            // valid msgId success
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"msgId\": 123}");
            baseJob.sendMsg();
            verify(msgBiz).execSend(123L);

            // valid msgId throws
            doThrow(new RuntimeException("send failed")).when(msgBiz).execSend(999L);
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"msgId\": 999}");
            baseJob.sendMsg();
            mocked.verify(() -> XxlJobHelper.handleFail("send failed"));
        }
    }

    @Test
    void testBaseJobPublishMsg() {
        BaseJob baseJob = new BaseJob(msgBiz, extendMsgService);

        try (MockedStatic<XxlJobHelper> mocked = Mockito.mockStatic(XxlJobHelper.class)) {
            // null param
            mocked.when(XxlJobHelper::getJobParam).thenReturn("");
            assertThrows(Exception.class, baseJob::publishMsg);

            // empty map
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{}");
            baseJob.publishMsg();

            // invalid msgId
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"foo\": \"bar\"}");
            baseJob.publishMsg();
            mocked.verify(() -> XxlJobHelper.handleFail(contains("msgId 参数无效")));

            // valid msgId success
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"msgId\": 456}");
            baseJob.publishMsg();
            verify(extendMsgService).publishNotice(456L);

            // valid msgId throws
            doThrow(new RuntimeException("publish error")).when(extendMsgService).publishNotice(888L);
            mocked.when(XxlJobHelper::getJobParam).thenReturn("{\"msgId\": 888}");
            baseJob.publishMsg();
            mocked.verify(() -> XxlJobHelper.handleFail("publish error"));
        }
    }

    @Test
    void testApplicationClass() {
        NoneExecutorServerApplication app = new NoneExecutorServerApplication();
        assertNotNull(app);
    }
}
