package com.paystream.apigateway.route;

import java.util.function.Consumer;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoute {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        // 각 서비스별 설정 메소드 호출
        addInventoryRoutes(routes);
        addNotificationRoutes(routes);
        addOrderRoutes(routes);
        addPaymentRoutes(routes);
        addUserRoutes(routes);

        return routes.build();
    }

    private Consumer<SpringCloudCircuitBreakerFilterFactory.Config> circuitBreakerConfig(
            String service) {
        return config ->
                config.setName("customCircuitBreaker")
                        .setFallbackUri("forward:/fallback/" + service);
    }

    // 1. Inventory Service
    private void addInventoryRoutes(RouteLocatorBuilder.Builder builder) {
        builder.route(
                        "inventory-service-swagger",
                        r ->
                                r.path("/api/inventories/v3/api-docs/**")
                                        .and()
                                        .method("GET")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                "/api/inventories/(?<path>.*)",
                                                                "/${path}"))
                                        .uri("lb://inventory-service"))
                .route(
                        "inventory-service-stores",
                        r ->
                                r.path("/api/stores/**", "/api/stores")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/stores(?<segment>.*)",
                                                                        "/stores${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "inventory-service")
                                                                .circuitBreaker(
                                                                        circuitBreakerConfig(
                                                                                "inventory-service")))
                                        .uri("lb://inventory-service"));
    }

    // 2. Notification Service
    private void addNotificationRoutes(RouteLocatorBuilder.Builder builder) {
        builder.route(
                        "notification-service-swagger",
                        r ->
                                r.path("/api/notifications/v3/api-docs/**")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/notifications/(?<segment>.*)",
                                                                        "/${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "notification-service"))
                                        .uri("lb://notification-service"))
                .route(
                        "notification-service",
                        r ->
                                r.path("/api/notifications/**", "/api/notifications/")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/notifications(?<segment>.*)",
                                                                        "/notifications${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "notification-service")
                                                                .circuitBreaker(
                                                                        circuitBreakerConfig(
                                                                                "notification-service")))
                                        .uri("lb://notification-service"));
    }

    // 3. Order Service
    private void addOrderRoutes(RouteLocatorBuilder.Builder builder) {
        builder.route(
                        "order-service-swagger",
                        r ->
                                r.path("/api/orders/v3/api-docs/**")
                                        .and()
                                        .method("GET")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/orders/(?<path>.*)",
                                                                        "/${path}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "user-service"))
                                        .uri("lb://order-service"))
                .route(
                        "order-service",
                        r ->
                                r.path("/api/orders/**", "/api/orders/")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/orders(?<segment>.*)",
                                                                        "/orders${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "order-service")
                                                                .circuitBreaker(
                                                                        circuitBreakerConfig(
                                                                                "order-service")))
                                        .uri("lb://order-service"));
    }

    // 4. Payment Service
    private void addPaymentRoutes(RouteLocatorBuilder.Builder builder) {
        builder.route(
                        "payment-service-swagger",
                        r ->
                                r.path("/api/payments/v3/api-docs/**")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/payments/(?<segment>.*)",
                                                                        "/${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "payment-service"))
                                        .uri("lb://payment-service"))
                .route(
                        "payment-service",
                        r ->
                                r.path("/api/payments/**", "/api/payments/")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/payments(?<segment>.*)",
                                                                        "/payments${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "payment-service")
                                                                .circuitBreaker(
                                                                        circuitBreakerConfig(
                                                                                "inventory-service")))
                                        .uri("lb://payment-service"));
    }

    // 5. User Service
    private void addUserRoutes(RouteLocatorBuilder.Builder builder) {
        builder.route(
                        "user-service-swagger",
                        r ->
                                r.path("/api/users/v3/api-docs/**")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/users/(?<segment>.*)",
                                                                        "/${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "user-service"))
                                        .uri("lb://user-service"))
                .route(
                        "user-service",
                        r ->
                                r.path("/api/users/**", "/api/users/")
                                        .filters(
                                                f ->
                                                        f.rewritePath(
                                                                        "/api/users(?<segment>.*)",
                                                                        "/users${segment}")
                                                                .addRequestHeader(
                                                                        "X-Service-Name",
                                                                        "user-service")
                                                                .circuitBreaker(
                                                                        circuitBreakerConfig(
                                                                                "user-service")))
                                        .uri("lb://user-service"));
    }
}
