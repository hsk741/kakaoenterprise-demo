package com.kakao.enterprise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.enterprise.dto.KakaoLoginUserProfile;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.dto.OAuth2ClientProviderProperties;
import com.kakao.enterprise.exception.TokenAcquisitionFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2RestService {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final OAuth2ClientProviderProperties oAuth2ClientProviderProperties;

    public OAuth2AccessToken getOAuth2TokenFromTokenEndpoint(String code) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", oAuth2ClientProviderProperties.getGrantType());
        body.add("client_id", oAuth2ClientProviderProperties.getClientId());
        body.add("client_secret", oAuth2ClientProviderProperties.getClientSecret());
        body.add("redirect_uri", oAuth2ClientProviderProperties.getRedirectUri());
        body.add("code", code);

        OAuth2AccessToken oAuth2AccessToken = authorizeUserFromAuthorizationServer(body, OAuth2AccessToken.class);

        return oAuth2AccessToken.toBuilder()
                .expiresAt(LocalDateTime.now().plusSeconds(oAuth2AccessToken.getExpiresIn()))
                .refreshTokenExpiresAt(LocalDateTime.now().plusSeconds(oAuth2AccessToken.getRefreshTokenExpiresIn()))
                .build();
    }

    public OAuth2AccessToken reissueAccessTokenFromTokenEndpoint(OAuth2AccessToken oAuth2AccessToken) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", oAuth2ClientProviderProperties.getRefreshTokenGrantType());
        body.add("client_id", oAuth2ClientProviderProperties.getClientId());
        body.add("refresh_token", oAuth2AccessToken.getRefreshToken());

        final OAuth2AccessToken reissuedOAuth2AccessToken = authorizeUserFromAuthorizationServer(body, OAuth2AccessToken.class);

//      재발급이므로 refreshToken, refreshTokenExpiresIn = null
        return reissuedOAuth2AccessToken.toBuilder()
                .expiresAt(LocalDateTime.now().plusSeconds(oAuth2AccessToken.getExpiresIn()))
                .build();
    }

    public KakaoLoginUserProfile getUserProfileFromProfileApiServer(OAuth2AccessToken oAuth2AccessToken) {
        return requestResourcesToApiServer("/v2/user/me", oAuth2AccessToken, KakaoLoginUserProfile.class);
    }

    public void logout(OAuth2AccessToken oAuth2AccessToken) {
        requestResourcesToApiServer("/v1/user/logout", oAuth2AccessToken, KakaoLoginUserProfile.class);
    }

    public void unlink(OAuth2AccessToken oAuth2AccessToken) {
        requestResourcesToApiServer("/v1/user/unlink", oAuth2AccessToken, KakaoLoginUserProfile.class);
    }

    private HttpEntity<MultiValueMap<String, String>> makeHttpEntity(MultiValueMap<String, String> body) {

        HttpHeaders headers = makeHttpHeaders();

        return (Objects.isNull(body) || body.isEmpty()) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> makeHttpEntity(OAuth2AccessToken oAuth2AccessToken) {

        HttpHeaders headers = makeHttpHeaders();
        headers.setBearerAuth(oAuth2AccessToken.getAccessToken());

        return new HttpEntity<>(headers);
    }

    private HttpHeaders makeHttpHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8));

        return headers;
    }

    private <T> T authorizeUserFromAuthorizationServer(MultiValueMap<String, String> body, Class<T> clazz) {

        final String url = UriComponentsBuilder.newInstance()
                .scheme(oAuth2ClientProviderProperties.getScheme())
                .host(oAuth2ClientProviderProperties.getAuthHost())
                .path(oAuth2ClientProviderProperties.getTokenPath()).build().toString();

        return restTemplate.postForObject(url, makeHttpEntity(body), clazz);
    }

    private <T> T requestResourcesToApiServer(String path, OAuth2AccessToken oAuth2AccessToken, Class<T> clazz) {

        final String url = UriComponentsBuilder.newInstance()
                .scheme(oAuth2ClientProviderProperties.getScheme())
                .host(oAuth2ClientProviderProperties.getApiHost())
                .path(path).build().toString();

        final ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, makeHttpEntity(oAuth2AccessToken), String.class);

        T t;

        try {
            t = objectMapper.readValue(responseEntity.getBody(), clazz);
        } catch (JsonProcessingException e) {
            throw new TokenAcquisitionFailureException(e);
        }

        return t;
    }
}
