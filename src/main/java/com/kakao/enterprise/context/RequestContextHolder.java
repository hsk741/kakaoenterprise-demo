package com.kakao.enterprise.context;


import org.springframework.core.NamedInheritableThreadLocal;

public class RequestContextHolder {

    private static final String CONTEXT_NAME = "Request Context";

    private static final ThreadLocal<RequestContext> INHERIABLE_REQUEST_CONTEXT_HOLDER = new NamedInheritableThreadLocal<RequestContext>(CONTEXT_NAME);

    private RequestContextHolder() {
    }

    public static RequestContext get() {

        RequestContext requestContext = INHERIABLE_REQUEST_CONTEXT_HOLDER.get();

        if (requestContext == null) {

            requestContext = new RequestContext();
            INHERIABLE_REQUEST_CONTEXT_HOLDER.set(requestContext);
        }

        return requestContext;
    }

    public static void clear() {

        if (INHERIABLE_REQUEST_CONTEXT_HOLDER.get() != null) {
            INHERIABLE_REQUEST_CONTEXT_HOLDER.remove();
        }
    }
}
