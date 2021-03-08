package com.gc.auth.core.authentication;

import com.gc.auth.core.annotation.TempToken;
import com.gc.auth.core.beans.AbstractBeanNameProvider;
import com.gc.auth.core.beans.UrlMappingProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author ShiZhongMing
 * 2021/3/8 17:55
 * @since 1.0
 */
public class DefaultTempTokenAuthenticationProviderImpl extends AbstractBeanNameProvider implements TempTokenAuthenticationProvider {

    private final UrlMappingProvider urlMappingProvider;

    public DefaultTempTokenAuthenticationProviderImpl(UrlMappingProvider urlMappingProvider) {
        this.urlMappingProvider = urlMappingProvider;
    }

    @Override
    public boolean validate(HttpServletRequest request, Authentication authentication) {
        if (!this.hasValidate(request)) {
            return true;
        }
        // 进行验证
        return false;
    }

    /**
     * 进行临时token验证
     * @param request HttpServletRequest
     * @return 结果
     */
    protected boolean doValidate(HttpServletRequest request) {
        return false;
    }

    /**
     * 判断是否需要验证
     * @param request HttpServletRequest
     * @return 结果
     */
    protected boolean hasValidate(HttpServletRequest request) {
        // 获取请求映射
        final UrlMappingProvider.UrlMapping urlMapping = this.urlMappingProvider.matchMapping(request);
        if (Objects.isNull(urlMapping)) {
            return false;
        }
        TempToken tempToken = AnnotationUtils.findAnnotation(urlMapping.getHandlerMethod().getMethod(), TempToken.class);
        if (Objects.isNull(tempToken)) {
            tempToken = AnnotationUtils.findAnnotation(urlMapping.getHandlerMethod().getBeanType(), TempToken.class);
        }
        return Objects.isNull(tempToken);
    }
}
