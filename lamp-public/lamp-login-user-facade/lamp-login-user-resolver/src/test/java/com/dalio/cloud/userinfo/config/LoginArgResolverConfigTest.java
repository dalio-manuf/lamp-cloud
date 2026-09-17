package com.dalio.cloud.userinfo.config;

import com.dalio.cloud.userinfo.resolver.ContextArgumentResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginArgResolverConfigTest {

    @Test
    @DisplayName("测试 LoginArgResolverConfig 注册参数解析器")
    void testAddArgumentResolvers() {
        LoginArgResolverConfig config = new LoginArgResolverConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        config.addArgumentResolvers(resolvers);

        assertEquals(1, resolvers.size());
        assertTrue(resolvers.get(0) instanceof ContextArgumentResolver);
    }
}
