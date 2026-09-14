package com.dalio.cloud.system.service.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.cloud.system.entity.system.DefClient;
import com.dalio.cloud.system.manager.system.DefClientManager;
import com.dalio.cloud.system.manager.system.DefParameterManager;
import com.dalio.cloud.system.service.system.impl.DefClientServiceImpl;
import com.dalio.cloud.system.service.system.impl.DefParameterServiceImpl;
import com.dalio.cloud.system.vo.save.system.DefClientSaveVO;
import com.dalio.cloud.system.vo.save.system.DefParameterSaveVO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 客户端服务与参数服务通用单元测试
 */
class DefSystemGeneralServiceTest {

    @Test
    @DisplayName("测试 DefClientServiceImpl 保存前随机密钥生成与客户端获取")
    void testDefClientServiceImpl() {
        DefClientManager clientManager = Mockito.mock(DefClientManager.class);
        DefClientServiceImpl clientService = new DefClientServiceImpl();
        ReflectionTestUtils.setField(clientService, "superManager", clientManager);

        // 1. saveBefore 生成随机 clientId 和 clientSecret
        DefClientSaveVO saveVO = new DefClientSaveVO();
        saveVO.setName("TestClient");
        DefClient clientEntity = (DefClient) ReflectionTestUtils.invokeMethod(clientService, "saveBefore", saveVO);
        assertNotNull(clientEntity);
        assertEquals("TestClient", clientEntity.getName());
        assertNotNull(clientEntity.getClientId());
        assertEquals(24, clientEntity.getClientId().length());
        assertNotNull(clientEntity.getClientSecret());
        assertEquals(32, clientEntity.getClientSecret().length());

        // 2. getClient 检索
        DefClient client = new DefClient();
        client.setClientId("app-123");
        when(clientManager.getClient("app-123", "secret-456")).thenReturn(client);
        assertSame(client, clientService.getClient("app-123", "secret-456"));
    }

    @Test
    @DisplayName("测试 DefParameterServiceImpl checkKey 唯一性校验与保存设置")
    void testDefParameterServiceImpl() {
        DefParameterManager paramManager = Mockito.mock(DefParameterManager.class);
        DefParameterServiceImpl paramService = new DefParameterServiceImpl();
        ReflectionTestUtils.setField(paramService, "superManager", paramManager);

        // 1. checkKey 唯一性
        when(paramManager.count(any())).thenReturn(1L);
        assertTrue(paramService.checkKey("SYS_KEY", 1L));

        when(paramManager.count(any())).thenReturn(0L);
        assertFalse(paramService.checkKey("NEW_KEY", 2L));

        // 2. saveBefore 注入 SYSTEM 默认参数类型
        DefParameterSaveVO saveVO = new DefParameterSaveVO();
        saveVO.setKey("MAX_LOGIN");
        saveVO.setName("最大登录数");
        Object entity = ReflectionTestUtils.invokeMethod(paramService, "saveBefore", saveVO);
        assertNotNull(entity);
    }
}
