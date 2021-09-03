package com.kakao.enterprise.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request,body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        log.debug(
                "===========================request begin================================================");
        log.debug("URI         : {}", request.getURI());
        log.debug("Method      : {}", request.getMethod());
        log.info("Headers     : {}", request.getHeaders());
        log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        log.debug(
                "==========================request end================================================");
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        log.debug(
                "============================response begin==========================================");
        log.debug("Status code  : {}", response.getStatusCode());
        log.debug("Status text  : {}", response.getStatusText());
        log.debug("Headers      : {}", response.getHeaders());
        log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
        log.debug(
                "=======================response end=================================================");
    }
}