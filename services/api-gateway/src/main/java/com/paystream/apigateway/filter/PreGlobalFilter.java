package com.paystream.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreGlobalFilter implements GlobalFilter, Ordered {

    // https://medium.com/@jojiapp/spring-gateway-reqeust-response-logging-c7a10a169ee9
    private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilterFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("#################################################################");

        // Duration
        exchange.getAttributes().put("startTime", System.currentTimeMillis());

        return modifyRequestBodyGatewayFilterFactory
                .apply(modifyRequestBodyGatewayFilterFactoryConfig())
                .filter(exchange, chain);
    }

    private static void logRequest(ServerHttpRequest request, String body) {
        log.info(
                "Request Id: {}, URI: {}, Headers: {}, QueryParams: {}, Body: {}",
                request.getId(),
                request.getURI(),
                request.getHeaders(),
                request.getQueryParams(),
                body);
    }

    private ModifyRequestBodyGatewayFilterFactory.Config
            modifyRequestBodyGatewayFilterFactoryConfig() {
        return new ModifyRequestBodyGatewayFilterFactory.Config()
                .setRewriteFunction(
                        String.class,
                        String.class,
                        (exchange, body) -> {
                            logRequest(exchange.getRequest(), body);
                            return Mono.justOrEmpty(body);
                        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
