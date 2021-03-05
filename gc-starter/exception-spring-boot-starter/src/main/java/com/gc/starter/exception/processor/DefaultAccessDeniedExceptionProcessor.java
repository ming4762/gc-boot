package com.gc.starter.exception.processor;

import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ShiZhongMing
 * 2021/3/4 15:22
 * @since 1.0
 */
public class DefaultAccessDeniedExceptionProcessor extends AbstractTypeExceptionMessageProcessor<AccessDeniedException> {
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object message(AccessDeniedException e, @Nullable HttpServletRequest request) {
        throw e;
    }
}
