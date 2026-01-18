package com.paystream.payment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Feign Client 설정 마이크로서비스 간 통신 시 X-Auth-User-Id 헤더를 전달합니다. */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String userId = request.getHeader("X-Auth-User-Id");

                    if (userId != null && !userId.isEmpty()) {
                        template.header("X-Auth-User-Id", userId);
                    }
                }
            }
        };
    }
}
