package com.dalio.cloud;

import com.dalio.cloud.common.ServerApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public class GeneratorServerApplicationTest {

    @Test
    @DisplayName("测试 GeneratorServerApplication 实例与 main 启动入口")
    void testApplicationInstance() {
        GeneratorServerApplication application = new GeneratorServerApplication();
        assertNotNull(application);

        try (MockedStatic<ServerApplication> mockedServer = mockStatic(ServerApplication.class)) {
            mockedServer.when(() -> ServerApplication.start(eq(GeneratorServerApplication.class), any(String[].class)))
                    .thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> GeneratorServerApplication.main(new String[]{}));
            mockedServer.verify(() -> ServerApplication.start(eq(GeneratorServerApplication.class), any(String[].class)));
        }
    }
}
