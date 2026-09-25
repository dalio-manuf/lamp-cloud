package com.dalio.cloud.base.config;

import com.dalio.cloud.file.properties.FileServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * FilePreviewConfiguration 单元测试
 *
 * @author went
 */
class FilePreviewConfigurationTest {

    @Test
    @DisplayName("测试 fileServerProperties 为 null 时不注册资源处理器")
    void testNullProperties() {
        FilePreviewConfiguration config = new FilePreviewConfiguration(null);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);

        assertDoesNotThrow(() -> config.addResourceHandlers(registry));
        verify(registry, never()).addResourceHandler(anyString());
    }

    @Test
    @DisplayName("测试 local 为 null 或路径为空时")
    void testNullOrEmptyLocal() {
        FileServerProperties properties = new FileServerProperties();
        properties.setLocal(null);

        FilePreviewConfiguration config = new FilePreviewConfiguration(properties);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);

        assertDoesNotThrow(() -> config.addResourceHandlers(registry));
        verify(registry, never()).addResourceHandler(anyString());

        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setPathPatterns("");
        local.setStoragePath("");
        properties.setLocal(local);

        assertDoesNotThrow(() -> config.addResourceHandlers(registry));
        verify(registry, never()).addResourceHandler(anyString());
    }

    @Test
    @DisplayName("测试 local 路径映射：未以 / 结尾和以 / 结尾")
    void testValidLocal() {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setPathPatterns("/files");
        local.setStoragePath("/data/upload");
        properties.setLocal(local);

        FilePreviewConfiguration config = new FilePreviewConfiguration(properties);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/files/**")).thenReturn(registration);
        when(registration.addResourceLocations("file:/data/upload/")).thenReturn(registration);

        assertDoesNotThrow(() -> config.addResourceHandlers(registry));
        verify(registry).addResourceHandler("/files/**");
        verify(registration).addResourceLocations("file:/data/upload/");

        // 测试已经以 / 结尾的情况
        local.setStoragePath("/data/upload/");
        assertDoesNotThrow(() -> config.addResourceHandlers(registry));
    }
}
