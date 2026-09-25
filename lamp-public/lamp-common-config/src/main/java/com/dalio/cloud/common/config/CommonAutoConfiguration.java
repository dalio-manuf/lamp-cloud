package com.dalio.cloud.common.config;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.aspect.LampLogAspect;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.common.properties.SystemProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author admin
 * @version v1.0
 * @date 2021/9/5 8:04 下午
 * @create [2021/9/5 8:04 下午 ] [admin] [初始创建]
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
@EnableConfigurationProperties({SystemProperties.class, IgnoreProperties.class})
public class CommonAutoConfiguration {
    private final SystemProperties systemProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SystemProperties.PREFIX, name = "recordLamp", havingValue = "true", matchIfMissing = true)
    public LampLogAspect getLampLogAspect() {
        return new LampLogAspect(systemProperties);
    }

    @PostConstruct
    public void init() {
        if (StrUtil.isNotEmpty(systemProperties.getCachePrefix())) {
            CacheKeyBuilder.Key.setPrefix(systemProperties.getCachePrefix());
            log.info("检查到配置文件中：{}.cachePrefix={}", SystemProperties.PREFIX, systemProperties.getCachePrefix());
        }
    }
}
