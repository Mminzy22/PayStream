package com.paystream.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostGlobalFilter implements GlobalFilter, Ordered {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = (long) exchange.getAttributes().get("startTime");
        long duration = System.currentTimeMillis() - startTime;

        //        return chain.filter(exchange)
        //                .then(
        //                        Mono.fromRunnable(
        //                                () -> {
        //                                    log.info(
        //                                            "[Trace-ID: {}] <<< Global Filter (POST):
        // Response Status Code -> {} | Duration: {}ms",
        //                                            traceId,
        //                                            exchange.getResponse().getStatusCode(),
        //                                            duration);
        //                                    log.info(
        //
        // "#################################################################");
        //                                }));
        return modifyResponseBodyGatewayFilterFactory
                .apply(modifyResponseGatewayFilterConfig())
                .filter(exchange, chain)
                .then(
                        Mono.fromRunnable(
                                () -> {
                                    log.info("Duration: {}ms", duration);
                                    log.info(
                                            "#################################################################");
                                }));
    }

    private static void logResponse(
            ServerHttpRequest request, ServerHttpResponse response, String body) {
        log.info(
                "Response Id: {}, URI: {}, StatusCode: {}, Headers: {}, body: {}",
                request.getId(),
                request.getURI(),
                response.getStatusCode(),
                response.getHeaders(),
                // 바디 내용은 너무 길 수 있으므로 별도로 출력하거나 길이 제한
                body.length() > 500 ? body.substring(0, 500) + "..." : body);
    }

    private ModifyResponseBodyGatewayFilterFactory.Config modifyResponseGatewayFilterConfig() {
        return new ModifyResponseBodyGatewayFilterFactory.Config()
                .setRewriteFunction(
                        String.class,
                        String.class,
                        (exchange, body) -> {
                            logResponse(exchange.getRequest(), exchange.getResponse(), body);
                            return Mono.justOrEmpty(body);
                        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
