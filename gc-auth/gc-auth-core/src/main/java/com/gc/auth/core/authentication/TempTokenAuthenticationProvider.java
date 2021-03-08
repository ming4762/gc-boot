package com.gc.auth.core.authentication;

import com.gc.auth.core.beans.BeanNameProvider;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * 临时token验证器
 * @author ShiZhongMing
 * 2021/3/8 17:51
 * @since 1.0
 */
public interface TempTokenAuthenticationProvider extends BeanNameProvider {

    /**
     * 验收临时 token
     * @param request HttpServletRequest
     * @param authentication Authentication
     * @return 验证结果
     */
    boolean validate(HttpServletRequest request, Authentication authentication);
}
