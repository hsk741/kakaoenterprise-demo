package com.kakao.enterprise.token;

import javax.servlet.http.HttpServletRequest;

public interface BearerTokenResolver {
    String resolve(HttpServletRequest request);
}
