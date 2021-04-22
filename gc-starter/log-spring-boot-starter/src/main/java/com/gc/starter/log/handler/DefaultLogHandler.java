package com.gc.starter.log.handler;

import com.gc.starter.log.annotation.Log;
import com.gc.starter.log.model.SysLogPO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author ShiZhongMing
 * 2021/4/22 13:46
 * @since 1.0
 */
public class DefaultLogHandler implements LogHandler {
    @Override
    public boolean save(@NonNull SysLogPO sysLog, @NonNull ProceedingJoinPoint point, @NonNull Log logAnnotation, long time, int code, @Nullable Object result, @Nullable String errorMessage) {
        return true;
    }
}
