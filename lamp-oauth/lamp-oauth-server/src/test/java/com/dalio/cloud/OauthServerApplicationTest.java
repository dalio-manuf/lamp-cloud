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
 * 认证服务启动类单元测试
 */
class OauthServerApplicationTest {

    @Test
    @DisplayName("测试 OauthServerApplication 构造与 main 启动入口")
    void testApplicationMain() {
        OauthServerApplication application = new OauthServerApplication();
        assertNotNull(application);

        try (MockedStatic<ServerApplication> mockedServer = mockStatic(ServerApplication.class)) {
            mockedServer.when(() -> ServerApplication.start(eq(OauthServerApplication.class), any(String[].class)))
                    .thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> OauthServerApplication.main(new String[]{}));
            mockedServer.verify(() -> ServerApplication.start(eq(OauthServerApplication.class), any(String[].class)));
        }
    }
}
