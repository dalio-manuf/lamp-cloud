package com.dalio.cloud.base;

import com.dalio.cloud.base.config.BootWebConfiguration;
import com.dalio.cloud.base.config.CorsConfiguration;
import com.dalio.cloud.base.config.FilePreviewConfiguration;
import com.dalio.cloud.base.config.WebSocketConfig;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.system.facade.DefResourceFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class BootConfigurationsTest {

    @Test
    @DisplayName("测试 WebSocketConfig 与 CorsConfiguration Bean 创建")
    void testBasicConfigurations() {
        WebSocketConfig wsConfig = new WebSocketConfig();
        ServerEndpointExporter exporter = wsConfig.serverEndpoint();
        assertNotNull(exporter);

        CorsConfiguration corsConfig = new CorsConfiguration();
        CorsFilter filter = corsConfig.corsFilter();
        assertNotNull(filter);
    }

    @Test
    @DisplayName("测试 FilePreviewConfiguration 资源映射逻辑")
    void testFilePreviewConfiguration() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        // 1. null fileServerProperties
        FilePreviewConfiguration previewConfig1 = new FilePreviewConfiguration(null);
        assertDoesNotThrow(() -> previewConfig1.addResourceHandlers(registry));

        // 2. null local
        FileServerProperties properties = new FileServerProperties();
        FilePreviewConfiguration previewConfig2 = new FilePreviewConfiguration(properties);
        assertDoesNotThrow(() -> previewConfig2.addResourceHandlers(registry));

        // 3. 有效 local 且路径不以 / 结尾
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setPathPatterns("/files");
        local.setStoragePath("/data/upload");
        properties.setLocal(local);
        FilePreviewConfiguration previewConfig3 = new FilePreviewConfiguration(properties);
        assertDoesNotThrow(() -> previewConfig3.addResourceHandlers(registry));

        // 4. 有效 local 且路径以 / 结尾
        local.setStoragePath("/data/upload/");
        assertDoesNotThrow(() -> previewConfig3.addResourceHandlers(registry));
    }

    @Test
    @DisplayName("测试 BootWebConfiguration 拦截器与视图控制器配置")
    void testBootWebConfiguration() {
        IgnoreProperties ignoreProperties = new IgnoreProperties();
        DefResourceFacade mockFacade = mock(DefResourceFacade.class);
        BootWebConfiguration webConfig = new BootWebConfiguration(ignoreProperties, mockFacade);
        org.springframework.test.util.ReflectionTestUtils.setField(webConfig, "profiles", "dev");

        HandlerInterceptor tokenInterceptor = webConfig.getTokenContextFilter();
        assertNotNull(tokenInterceptor);

        HandlerInterceptor saInterceptor = webConfig.getSaFilter();
        assertNotNull(saInterceptor);

        ResourceHandlerRegistry resourceRegistry = mock(ResourceHandlerRegistry.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> webConfig.addResourceHandlers(resourceRegistry));

        ViewControllerRegistry viewControllerRegistry = mock(ViewControllerRegistry.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> webConfig.addViewControllers(viewControllerRegistry));

        InterceptorRegistry interceptorRegistry = mock(InterceptorRegistry.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> webConfig.addInterceptors(interceptorRegistry));
    }
}
