package com.xxl.job.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.dalio.basic.validator.annotation.EnableFormValidator;
import com.dalio.cloud.common.ServerApplication;

import java.net.UnknownHostException;

import static com.dalio.cloud.common.constant.BizConstant.BUSINESS_PACKAGE;
import static com.dalio.cloud.common.constant.BizConstant.UTIL_PACKAGE;

/**
 * XXL-JOB 执行器启动类（单体/开发环境版）
 *
 * @author dalio
 */
@SpringBootApplication
@ComponentScan({
        UTIL_PACKAGE, BUSINESS_PACKAGE, "com.xxl.job.executor"
})
@EnableFormValidator
@Slf4j
public class NoneExecutorServerApplication extends ServerApplication {

    public static void main(String[] args) throws UnknownHostException {
        start(NoneExecutorServerApplication.class, args);
    }

}
