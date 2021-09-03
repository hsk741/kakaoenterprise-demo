package com.kakao.enterprise.util;

import com.kakao.enterprise.dto.MemberAuthentication;
import com.kakao.enterprise.dto.OAuth2AccessToken;
import com.kakao.enterprise.exception.TokenAcquisitionFailureException;

import javax.servlet.http.HttpSession;
import java.util.Optional;

public class SessionUtils {

    public static void removeSessionUserInfo(HttpSession session) {

        session.removeAttribute("oauthToken");
        session.removeAttribute("memberAuthentication");

        session.invalidate();
    }

    public static OAuth2AccessToken getAccessTokenFromSession(HttpSession session) {
        return Optional.ofNullable(session.getAttribute("oauthToken"))
                .map(oAuth2AccessToken -> (OAuth2AccessToken) oAuth2AccessToken)
                .orElseThrow(TokenAcquisitionFailureException::new);
    }

    public static MemberAuthentication getMemberAuthentcationFromSession(HttpSession session) {
        return Optional.ofNullable(session.getAttribute("memberAuthentication"))
                .map(memberAuthentication -> (MemberAuthentication) memberAuthentication)
                .orElseThrow(TokenAcquisitionFailureException::new);
    }
}
