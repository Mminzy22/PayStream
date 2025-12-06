package com.paystream.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PostGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getAttributes().get("traceId").toString();
        long startTime = (long) exchange.getAttributes().get("startTime");
        long duration = System.currentTimeMillis() - startTime;

        return chain.filter(exchange)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    log.info(
                                            "[Trace-ID: {}] <<< Global Filter (POST): Response Status Code -> {} | Duration: {}ms",
                                            traceId,
                                            exchange.getResponse().getStatusCode(),
                                            duration);
                                    log.info(
                                            "#################################################################");
                                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
