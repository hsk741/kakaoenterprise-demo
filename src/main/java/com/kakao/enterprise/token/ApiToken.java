package com.kakao.enterprise.token;

import org.springframework.util.Assert;

import java.time.Instant;

public class ApiToken extends BaseApiToken {

    private final String tokenType;

    public ApiToken(String tokenType, String tokenValue, Instant issuedAt, Instant expiresAt) {

        super(tokenValue, issuedAt, expiresAt);

        Assert.notNull(tokenType, "tokenType cannot be null");

        this.tokenType = tokenType;
    }
}
