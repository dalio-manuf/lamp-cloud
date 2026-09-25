package com.dalio.cloud.satoken.config;

import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.satoken.interceptor.NotAllowWriteInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 永远执行的配置
 *
 * @author admin
 * @date 2018/8/25
 */
@RequiredArgsConstructor
public class AlwaysConfigurer implements WebMvcConfigurer {
    private final SystemProperties systemProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NotAllowWriteInterceptor(systemProperties))
                .addPathPatterns("/**")
                .order(Integer.MAX_VALUE);
    }
}
