package com.gc.auth.core.authentication;

import com.gc.auth.core.constants.LoginTypeEnum;
import com.gc.auth.core.model.RestUserDetailsImpl;
import com.gc.common.i18n.utils.I18nUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 登录管理
 * @author shizhongming
 * 2020/1/23 8:17 下午
 */
public class RestAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider implements InitializingBean {

    private static final String USERNAME_PASSWORD_NULL = "用户名密码不能为空";

    private static final String USERNAME_PASSWORD_ERROR = "用户名密码错误";


    private static final String NONE_PROVIDED = "NONE_PROVIDED";

    private final UserDetailsService restUserDetailsService;

    public RestAuthenticationProvider(UserDetailsService restUserDetailsService) {
        this.restUserDetailsService = restUserDetailsService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {

        if (ObjectUtils.isEmpty(authentication.getCredentials())) {
            logger.debug("登录失败:密码不存在");
            throw new InternalAuthenticationServiceException(USERNAME_PASSWORD_NULL);
        }
        final String password = authentication.getCredentials().toString();
        if (!StringUtils.equals(userDetails.getPassword(), password)) {
            logger.debug("登录失败：密码错误");
            throw new InternalAuthenticationServiceException(I18nUtils.get("auth.exception.usernamePasswordError", USERNAME_PASSWORD_ERROR));
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
        String password = (String) authentication.getCredentials();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.equals(NONE_PROVIDED, username)) {
            throw new InternalAuthenticationServiceException(USERNAME_PASSWORD_NULL);
        }
        RestUserDetailsImpl user = (RestUserDetailsImpl) this.restUserDetailsService.loadUserByUsername(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new InternalAuthenticationServiceException(I18nUtils.get("auth.exception.usernamePasswordError", USERNAME_PASSWORD_ERROR));
        }
        user.setLoginType(LoginTypeEnum.USERNAME);
        return user;
    }
}
