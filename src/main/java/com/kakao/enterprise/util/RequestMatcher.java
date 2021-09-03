package com.kakao.enterprise.util;

import javax.servlet.http.HttpServletRequest;

public interface RequestMatcher {
    boolean matches(HttpServletRequest request);
}
