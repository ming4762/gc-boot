package com.gc.auth.core.i18n;

import com.gc.common.base.message.I18nMessage;

/**
 * @author ShiZhongMing
 * 2021/3/9 15:20
 * @since 1.0
 */
public enum I18nCodeEnum implements I18nMessage {
    /**
     * temp token为null
     */
    ERROR_TEMP_TOKEN_EMPTY("auth.error.tempToken.empty", "Temp Token validate fail，token is empty"),
    // 临时token过期
    ERROR_TEMP_TOKEN_EXPIRE("auth.error.tempToken.expire", "Temp Token validate fail，token is expire"),
    // temp token ip验证错误
    ERROR_TEMP_TOKEN_IP_FAIL("auth.error.tempToken.ipFail", "Temp Token validate fail： IP validate fail"),
    // 资源验证失败
    ERROR_TEMP_TOKEN_RESOURCE_FAIL("auth.error.tempToken.resourceFail", "Temp Token validate fail: resource validate fail"),
    // 用户名密码错误
    USERNAME_PASSWORD_ERROR("auth.error.usernamePasswordError", "username or password error"),
    // 用户名密码不能为null
    USERNAME_PASSWORD_NULL("auth.error.usernamePasswordEmpty", "username and password can not empty")
    ;


    private final String i18nCode;

    private final String defaultValue;

    I18nCodeEnum(String i18nCode, String defaultValue) {
        this.i18nCode = i18nCode;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getI18nCode() {
        return this.i18nCode;
    }

    @Override
    public String defaultMessage() {
        return this.defaultValue;
    }


}
