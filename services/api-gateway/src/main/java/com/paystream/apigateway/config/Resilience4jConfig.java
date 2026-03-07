package com.paystream.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customCircuitBreaker() {
        return factory ->
                factory.configureDefault(
                        id -> {
                            // 서킷 브레이커 상세 설정
                            CircuitBreakerConfig config =
                                    CircuitBreakerConfig.custom()
                                            .slidingWindowType(
                                                    CircuitBreakerConfig.SlidingWindowType
                                                            .COUNT_BASED)
                                            .slidingWindowSize(10) // 통계건수
                                            .minimumNumberOfCalls(2) // 최소요청횟수
                                            .failureRateThreshold(60) // 실패율 (이상치)
                                            .waitDurationInOpenState(
                                                    Duration.ofSeconds(10)) // Circuit Breaker 유지시간
                                            .build();

                            // 타임아웃 설정 (서비스가 죽지 않을 경우 설정된 타임아웃으로 연결을 끊어 Gateway의 리소스를 즉시 회수)
                            TimeLimiterConfig timeLimiterConfig =
                                    TimeLimiterConfig.custom()
                                            .timeoutDuration(Duration.ofSeconds(3)) // 전체 기본 타임아웃 3초
                                            .build();

                            // Resilience4JConfigBuilder를 사용하여 빌드 후 반환
                            return new Resilience4JConfigBuilder(id)
                                    .circuitBreakerConfig(config)
                                    .timeLimiterConfig(timeLimiterConfig)
                                    .build();
                        });
    }
}
