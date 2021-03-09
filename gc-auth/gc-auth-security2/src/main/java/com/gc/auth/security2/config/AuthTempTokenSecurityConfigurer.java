package com.gc.auth.security2.config;

import com.gc.auth.core.authentication.TempTokenAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

/**
 * @author ShiZhongMing
 * 2021/3/9 16:42
 * @since 1.0
 */
public class AuthTempTokenSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private TempTokenAuthenticationProvider tempTokenAuthenticationProvider;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        String access = String.format("@%s.validate(request, authentication)", this.tempTokenAuthenticationProvider.getBeanName());
        builder.authorizeRequests()
                .anyRequest()
                .access(access);
        super.init(builder);
    }

    @Autowired
    public void setTempTokenAuthenticationProvider(TempTokenAuthenticationProvider tempTokenAuthenticationProvider) {
        this.tempTokenAuthenticationProvider = tempTokenAuthenticationProvider;
    }
}
