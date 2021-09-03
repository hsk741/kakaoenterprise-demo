package com.kakao.enterprise.token;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.Instant;

@Getter
@EqualsAndHashCode
public abstract class BaseApiToken implements Serializable {

    private final String token;

    private final Instant issuedAt;

    private final Instant expiresAt;

    protected BaseApiToken(String token, @Nullable Instant issuedAt, @Nullable Instant expiresAt) {

        Assert.hasText(token, "tokenValue cannot be empty");
        if (issuedAt != null && expiresAt != null) {
            Assert.isTrue(expiresAt.isAfter(issuedAt), "expiresAt must be after issuedAt");
        }

        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}
