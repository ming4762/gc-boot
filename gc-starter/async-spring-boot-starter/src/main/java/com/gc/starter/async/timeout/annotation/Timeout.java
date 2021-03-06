package com.gc.starter.async.timeout.annotation;

import com.gc.starter.async.timeout.handler.DefaultTimeoutHandler;
import com.gc.starter.async.timeout.handler.TimeoutHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 超时注解
 * @author shizhongming
 * 2020/5/28 1:50 下午
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timeout {

    /**
     * 超时时间
     * @return 超时事件
     */
    long value() default Long.MAX_VALUE;

    /**
     * 执行器名称
     * handlerClass 优先级高
     * @return 执行器名称
     */
    String timeoutHandlerName() default "";

    /**
     * 执行器类型
     * @return 执行器类型
     */
    Class<? extends TimeoutHandler> handlerClass() default DefaultTimeoutHandler.class;
}
