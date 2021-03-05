package com.gc.starter.exception.processor;

import com.gc.common.base.http.HttpStatus;
import com.gc.common.base.message.Result;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * @author ShiZhongMing
 * 2021/3/5 10:31
 * @since 1.0
 */
public class DefaultConstraintViolationExceptionProcessor extends AbstractTypeExceptionMessageProcessor<ConstraintViolationException> {
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object message(ConstraintViolationException e, @Nullable HttpServletRequest request) {
        Set<ConstraintViolation<?>> constraintViolations =  e.getConstraintViolations();
        String message = constraintViolations.isEmpty() ? "未知异常" : constraintViolations.iterator().next().getMessage();
        return Result.failure(HttpStatus.BAD_REQUEST.getCode(), message);
    }
}
