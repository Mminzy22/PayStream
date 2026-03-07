package com.paystream.apigateway.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private Map<String, String> result;

    @GetMapping("/inventory-service")
    public Mono<Map<String, String>> inventoryServiceFallback() {
        result = new HashMap<>();
        result.put("info", "현재 인벤토리 서비스를 이용할 수 없습니다.");

        return Mono.just(result);
    }

    @GetMapping("/notification-service")
    public Mono<Map<String, String>> notificationServiceFallback() {
        result = new HashMap<>();
        result.put("info", "현재 알림 서비스를 이용할 수 없습니다.");

        return Mono.just(result);
    }

    @GetMapping("/order-service")
    public Mono<Map<String, String>> orderServiceFallback() {
        result = new HashMap<>();
        result.put("info", "현재 주문 서비스를 이용할 수 없습니다.");

        return Mono.just(result);
    }

    @GetMapping("/payment-service")
    public Mono<Map<String, String>> paymentServiceFallback() {
        result = new HashMap<>();
        result.put("info", "현재 결제 서비스를 이용할 수 없습니다.");

        return Mono.just(result);
    }

    @GetMapping("/user-service")
    public Mono<Map<String, String>> userServiceFallback() {
        result = new HashMap<>();
        result.put("info", "현재 유저 서비스를 이용할 수 없습니다.");

        return Mono.just(result);
    }
}
