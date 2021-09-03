package com.kakao.enterprise.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "oauth2.client.provider.kakao")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class OAuth2ClientProviderProperties {

    private final String scheme;
    private final String authHost;
    private final String authPath;
    private final String tokenPath;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String responseType;
    private final String grantType;
    private final String refreshTokenGrantType;
    private final String scope;

    private final String apiHost;
}

