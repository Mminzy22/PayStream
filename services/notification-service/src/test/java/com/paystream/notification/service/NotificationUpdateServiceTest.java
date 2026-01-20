package com.paystream.notification.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.dto.NotificationUpdateRequest;
import com.paystream.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** NotificationUpdateService 통합 테스트 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false",
            "spring.task.scheduling.enabled=false" // 테스트에서 스케줄러 비활성화
        })
class NotificationUpdateServiceTest {

    @Autowired private NotificationUpdateService notificationUpdateService;

    @Autowired private NotificationRepository notificationRepository;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("알림 상태 업데이트 - 성공")
    void updateShouldUpdateStatus() {
        // given
        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setTitle("테스트 알림");
        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);

        NotificationUpdateRequest request =
                NotificationUpdateRequest.builder().status(NotificationStatus.SENT).build();

        // when
        NotificationResponse response = notificationUpdateService.update(saved.getId(), request);

        // then
        assert response.getStatus() == NotificationStatus.SENT;

        Notification updated = notificationRepository.findById(saved.getId()).orElseThrow();
        assert updated.getStatus() == NotificationStatus.SENT;
    }

    @Test
    @DisplayName("알림 상태 업데이트 - 존재하지 않는 ID 업데이트 시 예외 발생")
    void updateShouldThrowExceptionWhenNotFound() {
        // given
        Long nonExistentId = 999L;
        NotificationUpdateRequest request =
                NotificationUpdateRequest.builder().status(NotificationStatus.SENT).build();

        // when & then
        try {
            notificationUpdateService.update(nonExistentId, request);
            assert false : "예외가 발생해야 합니다";
        } catch (PayStreamException e) {
            assert e.getError() == ExceptionEnum.NOTIFICATION_NOT_FOUND;
        }
    }
}
