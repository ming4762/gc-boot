package com.gc.auth.security2.config;

import com.gc.auth.security2.token.TempTokenValidateInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ShiZhongMing
 * 2021/3/10 12:38
 * @since 1.0
 */
@Configuration
public class AuthTempTokenBeanConfig {

    @Bean
    @ConditionalOnMissingBean(TempTokenValidateInterceptor.class)
    public TempTokenValidateInterceptor tempTokenValidateInterceptor() {
        return new TempTokenValidateInterceptor();
    }
}
