package com.gc.auth.security2.controller;

import com.gc.auth.core.annotation.NonUrlCheck;
import com.gc.auth.core.data.RestUserDetails;
import com.gc.auth.core.exception.AuthException;
import com.gc.auth.core.model.TempTokenData;
import com.gc.auth.core.service.AuthCache;
import com.gc.auth.core.utils.AuthUtils;
import com.gc.common.base.message.Result;
import com.gc.common.base.utils.IdGenerator;
import com.gc.common.base.utils.IpUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author shizhongming
 * 2020/4/24 9:17 上午
 */
@RequestMapping
@RestController
public class AuthController {

    @Autowired
    private AuthCache<String, Object> authCache;

    /**
     * 验证用户是否登录
     * @return 是否登录
     */
    @PostMapping("auth/isLogin")
    @NonUrlCheck
    public Result<Boolean> isLogin() {
        RestUserDetails restUserDetails = AuthUtils.getCurrentUser();
        return Result.success(ObjectUtils.isNotEmpty(restUserDetails));
    }

    /**
     * 注册临时token
     * @param tempTokenData 注册数据
     * @return 临时token
     */
    @PostMapping("auth/tempToken/register")
    @NonUrlCheck
    public Result<String> registerTempToken(@RequestBody TempTokenData tempTokenData, HttpServletRequest request) {
        final RestUserDetails user = AuthUtils.getNonNullCurrentUser();
        if (Objects.isNull(tempTokenData) || StringUtils.isBlank(tempTokenData.getResource())) {
            throw new AuthException("未设置申请资源");
        }
        // 验证权限
        boolean hasPermission = user.getPermissions().stream().anyMatch(item -> item.getAuthority().equals(tempTokenData.getResource()));
        if (!hasPermission) {
            throw new AuthException("没有权限，申请失败");
        }
        // 设置IP地址
        tempTokenData.setUserId(user.getUserId());
        tempTokenData.setIp(IpUtils.getIpAddr(request));

        String key = IdGenerator.nextId() + "";
        // 默认有效期60秒
        this.authCache.put(key, tempTokenData, 60);
        return Result.success(key);
    }
}
