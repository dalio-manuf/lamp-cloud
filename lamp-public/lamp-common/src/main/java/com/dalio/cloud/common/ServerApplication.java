package com.dalio.cloud.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * 服务启动工具类
 *
 * @author admin
 */
@Slf4j
public class ServerApplication {

    protected ServerApplication() {
    }

    public static void start(Class<?> primarySource, String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(primarySource, args);
        Environment env = application.getEnvironment();
        String hostAddress = "127.0.0.1";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("未能获取本机IP地址，使用 127.0.0.1 替代: {}", e.getMessage());
        }
        String port = env.getProperty("server.port", "8080");
        String msg = """
                
                ----------------------------------------------------------
                应用 '{}' 启动成功， JDK版本号：{} ！
                Swagger接口文档: http://{}:{}{}/swagger-ui.html
                数据库监控（可用于排查数据源是否链接成功）:   http://{}:{}/druid
                当前环境变量：{} 日志路径：{}
                ----------------------------------------------------------
                """;

        log.info(msg,
                env.getProperty("spring.application.name"),
                env.getProperty("java.version"),
                hostAddress,
                port,
                env.getProperty("server.servlet.context-path", ""),
                "127.0.0.1",
                port,
                env.getProperty("spring.profiles.active"), env.getProperty("LOG_PATH")
        );
    }
}
