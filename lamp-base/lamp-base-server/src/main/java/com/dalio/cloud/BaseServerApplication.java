package com.dalio.cloud;

import com.dalio.basic.validator.annotation.EnableFormValidator;
import com.dalio.cloud.common.ServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.net.UnknownHostException;

import static com.dalio.cloud.common.constant.BizConstant.BUSINESS_PACKAGE;
import static com.dalio.cloud.common.constant.BizConstant.UTIL_PACKAGE;

/**
 * 基础服务启动类
 *
 * @author admin
 * @date 2021-10-08
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@EnableFeignClients(value = {
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
@EnableFormValidator
public class BaseServerApplication extends ServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        start(BaseServerApplication.class, args);
    }
}
