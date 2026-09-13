package com.dalio.cloud.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.dalio.cloud.file.properties.FileServerProperties;

/**
 * 通过springboot 机制预览 文件
 * @author admin
 * @since 2025/3/20 16:38
 */
@Configuration
@Slf4j
public class FilePreviewConfiguration implements WebMvcConfigurer {
    @Autowired
    private FileServerProperties fileServerProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
        FileServerProperties.Local local = fileServerProperties.getLocal();

        registry.addResourceHandler(local.getPathPatterns() + "/**").addResourceLocations("file:" + local.getStoragePath());
    }
}
