package com.dalio.cloud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class MonitorServerApplicationTest {

    @Test
    @DisplayName("测试 MonitorServerApplication 构造与 main 启动入口")
    void testMain() {
        MonitorServerApplication app = new MonitorServerApplication();
        assertNotNull(app);

        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
        ConfigurableEnvironment mockEnv = mock(ConfigurableEnvironment.class);
        when(mockContext.getEnvironment()).thenReturn(mockEnv);
        when(mockEnv.getProperty("spring.application.name")).thenReturn("lamp-monitor");
        when(mockEnv.getProperty("server.port")).thenReturn("8761");
        when(mockEnv.getProperty("server.servlet.context-path", "")).thenReturn("");

        try (MockedStatic<SpringApplication> mockedSpring = mockStatic(SpringApplication.class)) {
            mockedSpring.when(() -> SpringApplication.run(eq(MonitorServerApplication.class), any(String[].class)))
                    .thenReturn(mockContext);

            assertDoesNotThrow(() -> MonitorServerApplication.main(new String[]{}));
            mockedSpring.verify(() -> SpringApplication.run(eq(MonitorServerApplication.class), any(String[].class)));
        }
    }
}
