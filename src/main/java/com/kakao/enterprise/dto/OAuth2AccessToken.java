package com.kakao.enterprise.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AccessToken {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private int expiresIn;

    private LocalDateTime expiresAt;

    private int refreshTokenExpiresIn;

    private LocalDateTime refreshTokenExpiresAt;

    private String scope;
}