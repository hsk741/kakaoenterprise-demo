package com.kakao.enterprise;

import com.kakao.enterprise.interceptor.BearerTokenRequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class KakaoEnterpriseDemoApplication implements WebMvcConfigurer {

    private final BearerTokenRequestInterceptor bearerTokenRequestInterceptor;

    public static void main(String[] args) {
        SpringApplication.run(KakaoEnterpriseDemoApplication.class, args);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(bearerTokenRequestInterceptor)
                .addPathPatterns("/api/**");
    }
}
