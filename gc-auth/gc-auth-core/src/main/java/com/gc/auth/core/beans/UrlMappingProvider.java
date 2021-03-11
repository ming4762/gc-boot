package com.gc.auth.core.beans;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ShiZhongMing
 * 2021/3/8 17:41
 * @since 1.0
 */
public interface UrlMappingProvider {

    /**
     * 匹配Mapping信息
     * @param request 请求信息
     * @return 匹配的结果，如果没有匹配上则返回null
     */
    @Nullable
    public UrlMapping matchMapping(@NonNull HttpServletRequest request);


    /**
     * URL映射
     */
    @Getter
    @Setter
    static
    class UrlMapping {
        /**
         * 请求方法
         */
        private RequestMethod requestMethod;

        /**
         * 执行目标方法
         */
        private HandlerMethod handlerMethod;
    }
}
