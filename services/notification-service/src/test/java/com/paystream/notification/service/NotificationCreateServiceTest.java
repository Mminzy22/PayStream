package com.paystream.notification.service;

import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.dto.NotificationRequest;
import com.paystream.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** NotificationCreateService 통합 테스트 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
        })
class NotificationCreateServiceTest {

    @Autowired private NotificationCreateService notificationCreateService;

    @Autowired private NotificationRepository notificationRepository;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("알림 생성 - 즉시 전송")
    void createImmediateNotification() {
        // given
        NotificationRequest request =
                NotificationRequest.builder()
                        .userId(1L)
                        .channel(NotificationChannel.EMAIL)
                        .title("즉시 전송 알림")
                        .body("즉시 전송 알림 본문")
                        .build();

        // when
        Long notificationId = notificationCreateService.create(request);

        // then
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        assert notification.getUserId().equals(1L);
        assert notification.getChannel() == NotificationChannel.EMAIL;
        assert notification.getTitle().equals("즉시 전송 알림");
        assert notification.getStatus() == NotificationStatus.PENDING;
        assert notification.getScheduledAt() != null;
    }

    @Test
    @DisplayName("알림 생성 - 예약 전송")
    void createScheduledNotification() {
        // given
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        NotificationRequest request =
                NotificationRequest.builder()
                        .userId(1L)
                        .channel(NotificationChannel.EMAIL)
                        .title("예약 전송 알림")
                        .body("예약 전송 알림 본문")
                        .scheduledAt(scheduledTime)
                        .templateCode("TEMPLATE_001")
                        .build();

        // when
        Long notificationId = notificationCreateService.create(request);

        // then
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        assert notification.getScheduledAt().equals(scheduledTime);
        assert notification.getTemplateCode().equals("TEMPLATE_001");
        assert notification.getStatus() == NotificationStatus.PENDING;
    }
}
