package com.gc.common.i18n.utils;

import com.gc.common.base.message.I18nMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * I18N工具类
 * @author shizhongming
 * 2020/5/13 11:03 上午
 */
public class I18nUtils {

    private static MessageSource messageSource;

    private I18nUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 通过key获取I18N信息
     * @param key key
     * @return I18N信息
     */
    public static String get(String key, String defaultMessage) {
        return Optional.ofNullable(messageSource)
                .map(item -> item.getMessage(key, null, LocaleContextHolder.getLocale()))
                .orElse(defaultMessage);
    }

    /**
     * 通过Key获取I18N信息
     * @param key key
     * @return I18N信息
     */
    public static String get(String key) {
        validate();
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public static String get(I18nMessage i18nMessage) {
        validate();
        return messageSource.getMessage(i18nMessage.getI18nCode(), null, LocaleContextHolder.getLocale());
    }

    public static MessageSource getMessageSource() {
        return messageSource;
    }

    private static void validate() {
        Assert.notNull(messageSource, "messageSource cannot be null");
    }

    public static void setMessageSource(MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }
}
