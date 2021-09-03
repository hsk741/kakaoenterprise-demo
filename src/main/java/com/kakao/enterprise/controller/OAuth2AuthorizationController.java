package com.kakao.enterprise.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.enterprise.dto.*;
import com.kakao.enterprise.dto.KakaoLoginUserProfile.KakaoAccount;
import com.kakao.enterprise.exception.TokenAcquisitionFailureException;
import com.kakao.enterprise.service.MemberService;
import com.kakao.enterprise.service.OAuth2RestService;
import com.kakao.enterprise.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

import static com.kakao.enterprise.dto.KakaoLoginUserProfile.KakaoAccount.Profile;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthorizationController {

    private final OAuth2RestService oAuth2RestService;

    private final ObjectMapper objectMapper;

    private final OAuth2ClientProviderProperties oAuth2ClientProviderProperties;

    private final MemberService memberService;

    @GetMapping("/")
    public String loginForm() {
        return "index";
    }

    @GetMapping("/oauth2/auth/kakao")
    public String redirectToAuthorizationServerLoginForm() {

        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + UriComponentsBuilder.newInstance()
                .scheme(oAuth2ClientProviderProperties.getScheme())
                .host(oAuth2ClientProviderProperties.getAuthHost())
                .path(oAuth2ClientProviderProperties.getAuthPath())
                .queryParam("client_id", oAuth2ClientProviderProperties.getClientId())
                .queryParam("redirect_uri", oAuth2ClientProviderProperties.getRedirectUri())
                .queryParam("response_type", oAuth2ClientProviderProperties.getResponseType())
                .queryParam("scope", oAuth2ClientProviderProperties.getScope())
                .queryParam("state", "")
                .build();
    }

    @GetMapping("/callback")
    public String receiveAuthorizationCodeFromIdp(String code, HttpSession session) {

        final OAuth2AccessToken oAuthToken = oAuth2RestService.getOAuth2TokenFromTokenEndpoint(code);

        session.setAttribute("oauthToken", oAuthToken);

        final KakaoLoginUserProfile userProfile = oAuth2RestService.getUserProfileFromProfileApiServer(oAuthToken);
        final KakaoAccount kakaoAccount = userProfile.getKakaoAccount();
        final Profile profile = kakaoAccount.getProfile();

        final MemberAuthentication memberAuthentication = MemberAuthentication.builder()
                .id(userProfile.getId())
                .email(kakaoAccount.getEmail())
                .ageRange(kakaoAccount.getAgeRange())
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();

        memberService.signUp(oAuthToken, userProfile.getId(), memberAuthentication);

        session.setAttribute("memberAuthentication", memberAuthentication);

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profileForm() {
        return "profile";
    }

    @GetMapping("/oauth2/user/logout")
    public String logoutUser(HttpSession session) {

        final OAuth2AccessToken oauthToken = SessionUtils.getAccessTokenFromSession(session);

        oAuth2RestService.logout(oauthToken);
        SessionUtils.removeSessionUserInfo(session);

        return "redirect:/";
    }

    @GetMapping("/oauth2/user/{id}/unlink")
    public String unlinkUser(@PathVariable long id, HttpSession session) {

        final OAuth2AccessToken oauthToken = SessionUtils.getAccessTokenFromSession(session);

        memberService.deleteMemberById(id, oauthToken);

        SessionUtils.removeSessionUserInfo(session);

        return "redirect:/";
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public String reissueAccessToken(HttpClientErrorException e, HttpSession session, RedirectAttributes redirectAttributes) {

        log.info("OAuth2AuthorizationController HttpClientErrorException", e);

        final OAuth2AccessToken preAccessToken = SessionUtils.getAccessTokenFromSession(session);

        final ErrorApiResponse apiErrorResponse;
        try {
            apiErrorResponse = objectMapper.readValue(e.getResponseBodyAsString(), ErrorApiResponse.class);
        } catch (JsonProcessingException ex) {
            throw new TokenAcquisitionFailureException(ex);
        }

        final HttpStatus statusCode = e.getStatusCode();
        if (statusCode == HttpStatus.UNAUTHORIZED && apiErrorResponse.getCode() == -401) {

            final OAuth2AccessToken reissuedOAuth2AccessToken = oAuth2RestService.reissueAccessTokenFromTokenEndpoint(preAccessToken);
            session.setAttribute("oauthToken",
                    reissuedOAuth2AccessToken.toBuilder()
                            .expiresAt(LocalDateTime.now().plusSeconds(reissuedOAuth2AccessToken.getExpiresIn()))
                            .build());

            redirectAttributes.addFlashAttribute("msg", "토큰이 갱신되었습니다.");
        }

        return "redirect:/profile";
    }

    @ExceptionHandler(TokenAcquisitionFailureException.class)
    public String redirectLoginForm(TokenAcquisitionFailureException e) {

        log.error("OAuth2AuthorizationController TokenAcquisitionFailureException", e);

        return "redirect:/";
    }
}
