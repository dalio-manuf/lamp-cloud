package com.dalio.cloud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.dalio.cloud.common.ServerApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * BaseServerApplication 启动测试
 *
 * @author went
 */
class BaseServerApplicationTest {

    @Test
    @DisplayName("测试 BaseServerApplication 实例与 main 启动入口")
    void testApplicationInstance() {
        BaseServerApplication application = new BaseServerApplication();
        assertNotNull(application);

        try (MockedStatic<ServerApplication> mockedServer = mockStatic(ServerApplication.class)) {
            mockedServer.when(() -> ServerApplication.start(eq(BaseServerApplication.class), any(String[].class)))
                    .thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> BaseServerApplication.main(new String[]{}));
            mockedServer.verify(() -> ServerApplication.start(eq(BaseServerApplication.class), any(String[].class)));
        }
    }
}
