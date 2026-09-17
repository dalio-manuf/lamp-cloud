package com.dalio.cloud.oauth.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OpenFeign 客户端接口定义测试
 */
class OAuthCloudApiTest {

    @Test
    @DisplayName("测试各 Feign 客户端契约与注解存在性")
    void testFeignApiInterfaces() {
        Class<?>[] apis = new Class<?>[]{
                CaptchaApi.class,
                DictApi.class,
                LogApi.class,
                OrgApi.class,
                PositionApi.class
        };

        for (Class<?> api : apis) {
            assertTrue(api.isInterface(), api.getSimpleName() + " 必须为接口");
            FeignClient feignClient = api.getAnnotation(FeignClient.class);
            assertNotNull(feignClient, api.getSimpleName() + " 必须声明 @FeignClient 注解");
        }
    }
}
