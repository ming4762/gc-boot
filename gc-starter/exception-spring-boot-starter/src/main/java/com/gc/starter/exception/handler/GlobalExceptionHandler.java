package com.gc.starter.exception.handler;

import com.gc.auth.core.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常管理
 * @author shizhongming
 * 2020/2/15 7:12 下午
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    private final AsyncNoticeHandler asyncNoticeHandler;

    private final ExceptionMessageHandler exceptionMessageHandler;

    public GlobalExceptionHandler(AsyncNoticeHandler asyncNoticeHandler, ExceptionMessageHandler exceptionMessageHandler) {
        this.asyncNoticeHandler = asyncNoticeHandler;
        this.exceptionMessageHandler = exceptionMessageHandler;
    }


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object handlerException(Exception e, HttpServletRequest request) throws Exception {
        // 处理异常通知
        this.asyncNoticeHandler.noticeException(e, AuthUtils.getCurrentUser(), request);
        return this.exceptionMessageHandler.message(e, request);
    }
}
