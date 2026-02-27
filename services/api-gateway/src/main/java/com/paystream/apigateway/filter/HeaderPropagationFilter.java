package com.paystream.apigateway.filter;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HeaderPropagationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(
                        auth -> {
                            Map<String, Object> claims = (Map<String, Object>) auth.getPrincipal();
                            String userId = String.valueOf(claims.get("sub"));

                            // SecurityContext에 인증 정보가 있을 때
                            ServerHttpRequest request =
                                    exchange.getRequest()
                                            .mutate()
                                            .header("X-Auth-User-Id", userId)
                                            .build();

                            log.info(">>> [Authentication] USER_ID: {}", userId);

                            return chain.filter(exchange.mutate().request(request).build());
                        })
                .switchIfEmpty(chain.filter(exchange)); // 인증이 필요없는 Endpoint는 그냥 통과
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.AUTHENTICATION.getOrder() + 1;
    }
}
