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
public class PreGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    log.info(
                                            ">>>>> Authentication Filter (PRE): Request URI -> {}",
                                            exchange.getRequest().getURI());
                                }));
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
