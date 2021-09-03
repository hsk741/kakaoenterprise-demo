package com.kakao.enterprise.interceptor;

import com.kakao.enterprise.context.RequestContextHolder;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.exception.InvalidBearerTokenException;
import com.kakao.enterprise.service.OAuth2RestService;
import com.kakao.enterprise.token.DefaultBearerTokenResolver;
import com.kakao.enterprise.util.BearerTokenRequestMatcher;
import com.kakao.enterprise.util.RequestMatcher;
import com.kakao.enterprise.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BearerTokenRequestInterceptor implements HandlerInterceptor {

    private final RequestMatcher requestMatcher = new BearerTokenRequestMatcher(new DefaultBearerTokenResolver());

    private final OAuth2RestService oAuth2RestService;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!requestMatcher.matches(request))
            throwInvalidBearerTokenException(response);

        final HttpSession session = request.getSession(false);
        if (session == null) {

            response.sendRedirect("/");

            return false;
        }

        final OAuth2AccessToken accessTokenFromSession = SessionUtils.getAccessTokenFromSession(session);

        if (!accessTokenFromSession.getAccessToken().equals(RequestContextHolder.get().getToken()))
            throwInvalidBearerTokenException(response);

        if (LocalDateTime.now().isAfter(accessTokenFromSession.getExpiresAt())) {

            if (!Objects.isNull(accessTokenFromSession.getRefreshToken())) {

                final OAuth2AccessToken reissuedOAuth2AccessToken = oAuth2RestService.reissueAccessTokenFromTokenEndpoint(accessTokenFromSession);
                session.setAttribute("oauthToken",
                        reissuedOAuth2AccessToken.toBuilder()
                                .expiresAt(LocalDateTime.now().plusSeconds(reissuedOAuth2AccessToken.getExpiresIn()))
                                .build());
            } else {

//              재발급받은 accessToken이 만료되는 경우
                oAuth2RestService.logout(accessTokenFromSession);
                SessionUtils.removeSessionUserInfo(session);
                response.sendRedirect("/");

                return false;
            }
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContextHolder.clear();
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    private void throwInvalidBearerTokenException(HttpServletResponse response) {

        response.setStatus(HttpStatus.BAD_REQUEST.value());

        throw new InvalidBearerTokenException();
    }
}
