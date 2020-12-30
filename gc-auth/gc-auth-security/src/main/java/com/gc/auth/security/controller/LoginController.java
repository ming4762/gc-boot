package com.gc.auth.security.controller;

import com.gc.auth.core.constants.LoginTypeConstants;
import com.gc.auth.core.data.RestUserDetails;
import com.gc.auth.core.model.LoginResult;
import com.gc.auth.core.model.Permission;
import com.gc.auth.security.pojo.dto.UserLoginDTO;
import com.gc.auth.security.service.AuthService;
import com.gc.common.base.message.Result;
import com.google.common.collect.Sets;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * jwt登陆
 * @author jackson
 * 2020/2/14 10:22 下午
 */
@RequestMapping
@ResponseBody
public class LoginController {


    private final AuthenticationManager authenticationManager;

    private final AuthService authService;


    public LoginController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    /**
     * 登陆接口
     * @param parameter 登录参数
     * @return 登录结果
     */
    @PostMapping("public/auth/login")
    public Result<LoginResult> login(@RequestBody @Valid UserLoginDTO parameter) {
        return Result.success(this.doLogin(parameter, LoginTypeConstants.WEB));
    }

    /**
     * 移动端登录接口
     * @param parameter 登录参数
     * @return 登录结果
     */
    @PostMapping("public/auth/mobileLogin")
    public Result<LoginResult> mobileLogin(@RequestBody @Valid UserLoginDTO parameter) {
        return Result.success(this.doLogin(parameter, LoginTypeConstants.MOBILE));
    }

    /**
     * 登出接口
     * @return 登出结果
     */
    @PostMapping("auth/logout")
    public Result<Object> logout(HttpServletRequest request) {
        this.authService.logout(request);
        return Result.success(null, "登出成功");
    }


    /**
     * 执行登陆
     * @param parameter 登录参数
     * @return 登录结果
     */
    private LoginResult doLogin(UserLoginDTO parameter, LoginTypeConstants type) {
        final Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(parameter.getUsername(), parameter.getPassword()));

        final RestUserDetails userDetails = (RestUserDetails) authentication.getPrincipal();
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        final String jwt = this.authService.doLogin(authentication, parameter.getRemember(), type);
        return LoginResult.builder()
                .user(userDetails)
                .token(jwt)
                .roles(userDetails.getRoles())
                .permissions(
                        Optional.ofNullable(userDetails.getPermissions())
                        .map(item -> item.stream().map(Permission::getAuthority).collect(Collectors.toSet()))
                        .orElse(Sets.newHashSet())
                )
                .build();
    }

}
