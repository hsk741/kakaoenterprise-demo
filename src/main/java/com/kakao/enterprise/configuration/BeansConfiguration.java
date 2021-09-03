package com.kakao.enterprise.configuration;

import com.kakao.enterprise.interceptor.RequestResponseLoggingInterceptor;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class BeansConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, LogbookClientHttpRequestInterceptor interceptor) {
        return restTemplateBuilder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()))
//                .additionalInterceptors(new RequestResponseLoggingInterceptor())
                .additionalInterceptors(interceptor)
                .build();
    }
}