package com.dalio.cloud.base.config;

import com.dalio.cloud.base.interceptor.AuthenticationSaInterceptor;
import com.dalio.cloud.base.interceptor.TokenContextFilter;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.system.facade.DefResourceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 基础服务-Web配置
 *
 * @author admin
 * @date 2021-10-08
 */
@Configuration
@RequiredArgsConstructor
public class BootWebConfiguration implements WebMvcConfigurer {


    private final IgnoreProperties ignoreProperties;
    private final DefResourceFacade defResourceFacade;
    @Value("${spring.profiles.active:dev}")
    protected String profiles;

    @Bean
    public HandlerInterceptor getTokenContextFilter() {
        return new TokenContextFilter(profiles, ignoreProperties);
    }

    @Bean
    public HandlerInterceptor getSaFilter() {
        return new AuthenticationSaInterceptor(ignoreProperties, defResourceFacade);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * 注册 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] commonPathPatterns = getExcludeCommonPathPatterns();
        registry.addInterceptor(getTokenContextFilter())
                .addPathPatterns("/**")
                .order(5)
                .excludePathPatterns(commonPathPatterns);

        // 注册 Sa-Token 拦截器，定义详细认证规则
        registry.addInterceptor(getSaFilter()).addPathPatterns("/**").order(10).excludePathPatterns(commonPathPatterns);


        WebMvcConfigurer.super.addInterceptors(registry);
    }


    /**
     * 排除拦截的地址（静态资源、认证文档、健康检查等）
     */
    protected String[] getExcludeCommonPathPatterns() {
        return new String[]{
                "/*.css",
                "/*.js",
                "/*.html",
                "/*.ico",
                "/*.png",
                "/*.jpg",
                "/error",
                "/login",
                "/favicon.ico",
                "/doc.html",
                "/v2/api-docs",
                "/v2/api-docs-ext",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/actuator/**",

                "/",
                "/csrf",

                "/META-INF/resources/**",
                "/resources/**",
                "/static/**",
                "/public/**",
                "classpath:/META-INF/resources/**",
                "classpath:/resources/**",
                "classpath:/static/**",
                "classpath:/public/**",

                "/cache/**",
                "/swagger-ui.html**",
                "/swagger-ui/**"
        };
    }
}
