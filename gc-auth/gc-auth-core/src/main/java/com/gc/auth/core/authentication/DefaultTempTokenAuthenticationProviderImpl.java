package com.gc.auth.core.authentication;

import com.gc.auth.core.annotation.TempToken;
import com.gc.auth.core.beans.AbstractBeanNameProvider;
import com.gc.auth.core.beans.UrlMappingProvider;
import com.gc.auth.core.model.TempTokenData;
import com.gc.auth.core.service.AuthCache;
import com.gc.common.base.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author ShiZhongMing
 * 2021/3/8 17:55
 * @since 1.0
 */
@Slf4j
public class DefaultTempTokenAuthenticationProviderImpl extends AbstractBeanNameProvider implements TempTokenAuthenticationProvider {

    /**
     * 标识token的key
     */
    private static final String TEMP_TOKEN_KEY = "access-token";

    private UrlMappingProvider urlMappingProvider;

    private AuthCache<String, Object> authCache;


    @Override
    public boolean validate(HttpServletRequest request, Authentication authentication) {
        final TempToken tempToken = this.hasValidate(request);
        if (Objects.isNull(tempToken)) {
            return true;
        }
        // 进行验证
        return this.doValidate(request, tempToken);
    }

    /**
     * 进行临时token验证
     * @param request HttpServletRequest
     * @return 结果
     */
    protected boolean doValidate(@NonNull HttpServletRequest request, @NonNull TempToken tempToken) {
        // 获取临时token
        String token = request.getParameter(TEMP_TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            log.warn("Temp Token validate fail，token is null");
            return false;
        }
        // 从缓存中获取信息
        final TempTokenData data = (TempTokenData) this.authCache.get(token);
        if (Objects.isNull(data)) {
            log.warn("Temp Token validate fail，token is expire");
            return false;
        }
        // IP 验证失败
        if (tempToken.ipValidate() && !StringUtils.equals(IpUtils.getIpAddr(request), data.getIp())) {
            log.warn("Temp Token validate fail: IP validate fail, register IP:[{}], request IP: [{}]", data.getIp(), IpUtils.getIpAddr(request));
            return false;
        }
        // 验证资源是否匹配
        if (!StringUtils.equals(tempToken.resource(), data.getResource())) {
            log.warn("Temp Token validate fail: resource validate fail, register resource:[{}], request resource: [{}]", data.getResource(), tempToken.resource());
            return false;
        }
        return true;
    }

    /**
     * 判断是否需要验证
     * @param request HttpServletRequest
     * @return 结果
     */
    protected TempToken hasValidate(HttpServletRequest request) {
        // 获取请求映射
        final UrlMappingProvider.UrlMapping urlMapping = this.urlMappingProvider.matchMapping(request);
        if (Objects.isNull(urlMapping)) {
            return null;
        }
        TempToken tempToken = AnnotationUtils.findAnnotation(urlMapping.getHandlerMethod().getMethod(), TempToken.class);
        if (Objects.isNull(tempToken)) {
            tempToken = AnnotationUtils.findAnnotation(urlMapping.getHandlerMethod().getBeanType(), TempToken.class);
        }
        return tempToken;
    }

    @Autowired
    public void setAuthCache(AuthCache<String, Object> authCache) {
        this.authCache = authCache;
    }

    @Autowired
    public void setUrlMappingProvider(UrlMappingProvider urlMappingProvider) {
        this.urlMappingProvider = urlMappingProvider;
    }
}
