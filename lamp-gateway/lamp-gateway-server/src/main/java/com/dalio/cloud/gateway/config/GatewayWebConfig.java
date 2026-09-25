package com.dalio.cloud.gateway.config;

import com.dalio.basic.converter.String2DateConverter;
import com.dalio.basic.converter.String2LocalDateConverter;
import com.dalio.basic.converter.String2LocalDateTimeConverter;
import com.dalio.basic.converter.String2LocalTimeConverter;
import com.dalio.basic.jackson.LampJacksonModule;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.common.properties.SystemProperties;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.dalio.basic.utils.DateUtils.DEFAULT_DATE_TIME_FORMAT;

/**
 * 网关 Web 与 Jackson 序列化配置
 *
 * @author dalio
 * @date 2021/12/9 18:52
 */
@Configuration
@EnableConfigurationProperties({SystemProperties.class, IgnoreProperties.class})
public class GatewayWebConfig {

    /**
     * Spring 工具类
     *
     * @param applicationContext 上下文
     */
    @Bean
    public SpringUtils getSpringUtils(ApplicationContext applicationContext) {
        SpringUtils instance = SpringUtils.getInstance();
        SpringUtils.setApplicationContext(applicationContext);
        return instance;
    }

    /**
     * 全局配置 序列化和反序列化规则
     *
     * @param builder 构造器
     * @return 全局 ObjectMapper
     */
    @Bean
    @Primary
    @ConditionalOnClass(ObjectMapper.class)
    @ConditionalOnMissingBean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper
                .setLocale(Locale.CHINA)
                //去掉默认的时间戳格式
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                // 时区
                .setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
                //Date参数日期格式
                .setDateFormat(new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.CHINA))

                //该特性决定parser是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）。 如果该属性关闭，则如果遇到这些字符，则会抛出异常。JSON标准说明书要求所有控制符必须使用引号，因此这是一个非标准的特性
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
                // 忽略不能转义的字符
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
                //在使用spring boot + jpa/hibernate，如果实体字段上加有FetchType.LAZY，并使用jackson序列化为json串时，会遇到SerializationFeature.FAIL_ON_EMPTY_BEANS异常
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                //忽略未知字段
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                //单引号处理
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true);
        // 注册自定义模块
        objectMapper.registerModule(new LampJacksonModule()).findAndRegisterModules();

        return objectMapper;
    }

    /**
     * 解决 @RequestParam(value = "date") Date date
     * date 类型参数 格式问题
     */
    @Bean
    public Converter<String, Date> dateConvert() {
        return new String2DateConverter();
    }

    /**
     * 解决 @RequestParam(value = "time") LocalDate time
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new String2LocalDateConverter();
    }

    /**
     * 解决 @RequestParam(value = "time") LocalTime time
     */
    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        return new String2LocalTimeConverter();
    }

    /**
     * 解决 @RequestParam(value = "time") LocalDateTime time
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new String2LocalDateTimeConverter();
    }

}
