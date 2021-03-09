package com.gc.auth.security2.filter;

import com.gc.auth.core.data.RestUserDetails;
import com.gc.auth.core.utils.AuthUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 临时token注册过滤器
 * @author ShiZhongMing
 * 2021/3/9 17:00
 * @since 1.0
 */
public class AuthTempTokenRegisterFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final RestUserDetails user = AuthUtils.getCurrentUser();
        // 获取用户信息验证是否拥有权限
    }
}
