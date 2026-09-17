package com.xxl.job.executor.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.xxl.job.executor.NoneExecutorServerApplication;

@Disabled("需要外部XXL-Job Admin与数据库运行环境，不作为自动化单元测试运行")
@SpringBootTest(classes = NoneExecutorServerApplication.class)
public class XxlJobExecutorExampleBootApplicationTests {

    @Test
    public void contextLoads() {
    }

}
