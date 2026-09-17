package com.dalio.cloud.loginuser.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginUserApiContractTest {

    @Test
    @DisplayName("测试 Feign 客户端接口契约定义")
    void testFeignInterfaces() {
        assertTrue(BaseApi.class.isInterface());
        assertTrue(OauthApi.class.isInterface());
        assertTrue(SystemApi.class.isInterface());

        assertNotNull(BaseApi.class.getAnnotation(FeignClient.class));
        assertNotNull(OauthApi.class.getAnnotation(FeignClient.class));
        assertNotNull(SystemApi.class.getAnnotation(FeignClient.class));
    }
}
