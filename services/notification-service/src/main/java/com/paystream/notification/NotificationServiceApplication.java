package com.paystream.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 알림 서비스 메인 애플리케이션
 * - Eureka Client로 등록되어 서비스 디스커버리 활용
 * - Kafka 이벤트 기반 알림 처리 (추후 구현)
 */
@EnableDiscoveryClient
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}

