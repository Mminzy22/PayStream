package com.paystream.notification;

import com.paystream.notification.service.NotificationSendService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** NotificationService 애플리케이션 통합 테스트 */
@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false",
            "spring.task.scheduling.enabled=false" // 테스트에서 스케줄러 비활성화
        })
class NotificationServiceApplicationTests {

    @MockitoBean private NotificationSendService notificationSendService;

    /** 스프링 컨텍스트 로딩 테스트 - 애플리케이션이 정상적으로 기동되는지 확인 */
    @Test
    void contextLoads() {
        // 컨텍스트가 정상적으로 로드되면 테스트 통과
    }
}
