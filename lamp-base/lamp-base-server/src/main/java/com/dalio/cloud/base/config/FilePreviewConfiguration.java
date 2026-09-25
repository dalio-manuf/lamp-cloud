package com.dalio.cloud.base.config;

import cn.hutool.core.util.StrUtil;
import com.dalio.cloud.file.properties.FileServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通过 Spring Boot 机制预览文件
 *
 * @author admin
 * @since 2025/3/20 16:38
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class FilePreviewConfiguration implements WebMvcConfigurer {

    private final FileServerProperties fileServerProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
        if (fileServerProperties == null) {
            return;
        }
        FileServerProperties.Local local = fileServerProperties.getLocal();
        if (local != null && StrUtil.isNotBlank(local.getPathPatterns()) && StrUtil.isNotBlank(local.getStoragePath())) {
            String storagePath = local.getStoragePath();
            if (!storagePath.endsWith("/")) {
                storagePath += "/";
            }
            registry.addResourceHandler(local.getPathPatterns() + "/**")
                    .addResourceLocations("file:" + storagePath);
            log.info("本地文件预览路径已映射: {}/** -> file:{}", local.getPathPatterns(), storagePath);
        }
    }
}
