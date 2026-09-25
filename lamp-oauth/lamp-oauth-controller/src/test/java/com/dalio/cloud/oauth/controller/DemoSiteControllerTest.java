package com.dalio.cloud.oauth.controller;

import com.dalio.basic.base.R;
import com.dalio.cloud.oauth.service.UserInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DemoSiteController 单元测试
 */
class DemoSiteControllerTest {

    @Test
    @DisplayName("测试 registerTempAdmin 注册临时管理员")
    void testRegisterTempAdmin() {
        UserInfoService userInfoService = Mockito.mock(UserInfoService.class);
        DemoSiteController controller = new DemoSiteController(userInfoService);

        Map<String, Object> map = Collections.singletonMap("username", "temp_admin");
        when(userInfoService.registerTempAdmin("DEF")).thenReturn(map);

        R<Map<String, Object>> result = controller.registerTempAdmin("DEF");
        assertNotNull(result);
        assertTrue(result.getIsSuccess());
        assertEquals("temp_admin", result.getData().get("username"));
        verify(userInfoService).registerTempAdmin("DEF");
    }
}
