package com.paystream.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class LoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String requestId = exchange.getRequest().getId();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.info(">>> [REQUEST] Request Id: {}, URI: {}, Method: {}", requestId, path, method);

        return chain.filter(exchange)
                .doFinally(
                        signalType -> {
                            long duration = System.currentTimeMillis() - startTime;
                            int statusCode =
                                    exchange.getResponse().getStatusCode() != null
                                            ? exchange.getResponse().getStatusCode().value()
                                            : 0;

                            log.info(
                                    ">>> [RESPONSE] Request Id: {}, URI: {}, Method: {}, Status: {}, Time: {}ms",
                                    requestId,
                                    path,
                                    method,
                                    statusCode,
                                    duration);
                        });
    }
}
