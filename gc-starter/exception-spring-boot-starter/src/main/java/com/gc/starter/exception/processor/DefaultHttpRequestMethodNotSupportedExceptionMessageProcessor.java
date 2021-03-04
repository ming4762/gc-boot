package com.gc.starter.exception.processor;

import com.gc.common.base.http.HttpStatus;
import com.gc.common.base.message.Result;
import com.gc.common.base.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ShiZhongMing
 * 2021/3/3 17:37
 * @since 1.0
 */
@Slf4j
public class DefaultHttpRequestMethodNotSupportedExceptionMessageProcessor extends AbstractI18nExceptionMessageProcessor<HttpRequestMethodNotSupportedException> {
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object message(HttpRequestMethodNotSupportedException e, @Nullable HttpServletRequest request) {
        log.error("HttpRequestMethodNotSupportedException: 当前请求方式 {}, 支持请求方式 {}", e.getMethod(), JsonUtils.toJsonString(e.getSupportedHttpMethods()));
        final String message = this.i18nMessage(HttpStatus.METHOD_NOT_ALLOWED, e.toString(), e.getMethod(), JsonUtils.toJsonString(e.getSupportedHttpMethods()));
        return Result.failure(message);
    }
}
