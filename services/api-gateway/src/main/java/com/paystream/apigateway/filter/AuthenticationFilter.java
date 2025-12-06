package com.paystream.apigateway.filter;

import com.paystream.apigateway.properties.WhitelistProperties;
import com.paystream.apigateway.util.JwtUtil;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final WhitelistProperties whitelistProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getAttributes().get("traceId").toString();
        ServerHttpRequest request = exchange.getRequest();

        boolean isWhitelist = isWhitelist(request);
        if (isWhitelist) {
            return chain.filter(exchange);
        }

        // JWT 토큰 검증
        String token = extractToken(exchange);
        if (token == null || !jwtUtil.validateToken(token, "access")) {
            return onError(exchange, "Invalid access token.", HttpStatus.UNAUTHORIZED);
        }

        try {
            Long userId = jwtUtil.extractUserId(token);

            ServerHttpRequest modifiedRequest =
                    request.mutate().header("X-Auth-User-Id", String.valueOf(userId)).build();

            log.info(
                    "[Trace-ID: {}] AuthenticationFilter SUCCESS | User-ID: {} | Forwarding with X-Auth-User-Id",
                    traceId,
                    userId);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            log.error("[AuthenticationFilter] Claims extraction error: {}", e.getMessage());
            return onError(exchange, "Failed to process token claims.", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isWhitelist(ServerHttpRequest request) {
        return whitelistProperties.getPaths().stream()
                .anyMatch(
                        whitelistPath ->
                                pathMatcher.match(
                                        "/**" + whitelistPath, request.getURI().getPath()));
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        String traceId = exchange.getAttributes().get("traceId").toString();
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseBody =
                String.format(
                        "{\"code\": \"%s\", \"message\": \"%s\"}",
                        httpStatus.value(), "토큰이 유효하지 않습니다.");
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        log.error("[Trace-ID: {}] Authentication FAILED: {} -> {}", traceId, httpStatus, err);

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
