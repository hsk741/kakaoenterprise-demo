package com.kakao.enterprise.token;

import com.kakao.enterprise.context.RequestContextHolder;
import com.kakao.enterprise.exception.InvalidBearerTokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DefaultBearerTokenResolver implements BearerTokenResolver {

    private static final Pattern authorizationPattern = Pattern.compile("^Bearer *([^ ]+) *$", Pattern.CASE_INSENSITIVE);
//    private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z]{3})$");

    @Override
    public String resolve(HttpServletRequest request) {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.startsWithIgnoreCase(authorization, "bearer")) {

            Matcher matcher = authorizationPattern.matcher(authorization);

            if (!matcher.matches()) {
                throw new InvalidBearerTokenException();
            }

            String token = matcher.group(1);

            RequestContextHolder.get().setToken(token);

            return token;
        }
        return null;
    }
}