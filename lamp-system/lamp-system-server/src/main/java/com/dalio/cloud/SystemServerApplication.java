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
 * https://mp.weixin.qq.com/s/zkxI5IQP0jFTjVYe5pTsXw
 * EnableAspectJAutoProxy(proxyTargetClass=true, exposeProxy=true) 配合 @EnableCaching
 * 才能解决在同一个类中通过 AopContext.currentProxy() 调用时，使缓存生效
 *
 * @author admin
 * @date 2018-01-13 1:34
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@EnableFeignClients(value = {
        UTIL_PACKAGE, BUSINESS_PACKAGE
})
@Slf4j
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)

@EnableFormValidator
public class SystemServerApplication extends ServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        start(SystemServerApplication.class, args);
    }

}
