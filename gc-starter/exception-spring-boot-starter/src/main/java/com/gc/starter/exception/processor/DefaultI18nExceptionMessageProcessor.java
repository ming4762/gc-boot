package com.gc.starter.exception.processor;

import com.gc.common.base.message.Result;
import com.gc.common.base.message.ResultCodeEnum;
import com.gc.common.i18n.exception.I18nException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ShiZhongMing
 * 2021/3/4 14:53
 * @since 1.0
 */
@Slf4j
public class DefaultI18nExceptionMessageProcessor extends AbstractI18nExceptionMessageProcessor<I18nException> {
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object message(I18nException e, @Nullable HttpServletRequest request) {
        log.error(String.format("DataManagerException: 状态码 %s, 异常信息 %s", e.getCode(), e.getMessage()), e.getE());
        return Result.failure(ResultCodeEnum.BUSINESS_ERROR.getCode(), this.i18nMessage(e.getI18nMessage(), null));
    }
}
