package com.kakao.enterprise.util;

import com.kakao.enterprise.exception.InvalidBearerTokenException;
import com.kakao.enterprise.token.BearerTokenResolver;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

public final class BearerTokenRequestMatcher implements RequestMatcher {

    private final BearerTokenResolver bearerTokenResolver;

    public BearerTokenRequestMatcher(BearerTokenResolver bearerTokenResolver) {

        Assert.notNull(bearerTokenResolver, "bearerTokenResolver cannot be null");
        this.bearerTokenResolver = bearerTokenResolver;
    }

    @Override
    public boolean matches(HttpServletRequest request) {

        try {
            return this.bearerTokenResolver.resolve(request) != null;
        } catch (InvalidBearerTokenException e) {
            return false;
        }
    }
}