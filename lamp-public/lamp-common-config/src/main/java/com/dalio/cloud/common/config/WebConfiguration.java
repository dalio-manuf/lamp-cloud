package com.dalio.cloud.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dalio.basic.boot.config.BaseConfig;
import com.dalio.basic.constant.Constants;
import com.dalio.basic.log.event.SysLogListener;
import com.dalio.cloud.oauth.facade.LogFacade;

/**
 * 基础服务-Web配置
 *
 * @author admin
 * @date 2021-10-08
 */
@Configuration
public class WebConfiguration extends BaseConfig {

    /**
     * lamp.log.enabled = true 并且 lamp.log.type=DB时实例该类
     */
    @Bean
    @ConditionalOnExpression("${" + Constants.PROJECT_PREFIX + ".log.enabled:true} && 'DB'.equals('${" + Constants.PROJECT_PREFIX + ".log.type:LOGGER}')")
    public SysLogListener sysLogListener(LogFacade logApi) {
        return new SysLogListener(logApi::save);
    }
}
