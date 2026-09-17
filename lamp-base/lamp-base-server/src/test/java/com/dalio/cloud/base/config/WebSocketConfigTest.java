package com.dalio.cloud.base.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * WebSocketConfig 单元测试
 *
 * @author went
 */
class WebSocketConfigTest {

    @Test
    @DisplayName("测试 WebSocketConfig 创建 ServerEndpointExporter")
    void testServerEndpoint() {
        WebSocketConfig config = new WebSocketConfig();
        ServerEndpointExporter exporter = config.serverEndpoint();
        assertNotNull(exporter);
    }
}
