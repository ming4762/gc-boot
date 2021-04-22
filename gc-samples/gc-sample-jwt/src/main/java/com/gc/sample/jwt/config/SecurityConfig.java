package com.gc.sample.jwt.config;

import com.gc.auth.core.handler.AuthAccessDeniedHandler;
import com.gc.auth.core.handler.RestAuthenticationEntryPoint;
import com.gc.auth.extensions.jwt.config.AuthJwtSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * @author ShiZhongMing
 * 2021/4/22 14:14
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.logout().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().csrf().disable().cors()
                .and().authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
                .and().exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .accessDeniedHandler(new AuthAccessDeniedHandler())
                .and()
                .apply(AuthJwtSecurityConfigurer.jwt())
                .serviceProvider()
                .applicationContext(this.getApplicationContext());

    }
}
