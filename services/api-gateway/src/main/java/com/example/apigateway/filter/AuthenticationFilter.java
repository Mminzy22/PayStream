package com.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    // 이 Config 클래스는 yml 파일에서 args를 받을 때 사용합니다. 지금은 비워도 된다.
    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            log.info(">>>>> Authentication Filter (PRE): Request URI -> {}", exchange.getRequest().getURI());

            log.info("인증 로직 처리");

            // 인증 성공시 다음 필터로 체인
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("<<<<< Authentication Filter (POST): Response Status Code -> {}", exchange.getResponse().getStatusCode());
            }));
        });
    }
}
