package com.paystream.notification.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/** NotificationFindService 통합 테스트 */
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
class NotificationFindServiceTest {

    @Autowired private NotificationFindService notificationFindService;

    @Autowired private NotificationRepository notificationRepository;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("알림 조회 - ID로 조회 성공")
    void findByIdShouldReturnNotification() {
        // given
        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setTitle("테스트 알림");
        notification.setBody("테스트 알림 본문");
        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);

        // when
        NotificationResponse response = notificationFindService.findById(saved.getId());

        // then
        assert response.getId().equals(saved.getId());
        assert response.getUserId().equals(1L);
        assert response.getChannel() == NotificationChannel.EMAIL;
        assert response.getTitle().equals("테스트 알림");
    }

    @Test
    @DisplayName("알림 조회 - 존재하지 않는 ID 조회 시 예외 발생")
    void findByIdShouldThrowExceptionWhenNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        try {
            notificationFindService.findById(nonExistentId);
            assert false : "예외가 발생해야 합니다";
        } catch (PayStreamException e) {
            assert e.getError() == ExceptionEnum.NOTIFICATION_NOT_FOUND;
        }
    }

    @Test
    @DisplayName("사용자별 알림 목록 조회 - 성공")
    void findByUserIdShouldReturnNotificationList() {
        // given
        Long userId = 1L;

        Notification notification1 = new Notification();
        notification1.setUserId(userId);
        notification1.setChannel(NotificationChannel.EMAIL);
        notification1.setTitle("알림 1");
        notification1.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setUserId(userId);
        notification2.setChannel(NotificationChannel.EMAIL);
        notification2.setTitle("알림 2");
        notification2.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(notification2);

        // when
        List<NotificationResponse> responses = notificationFindService.findByUserId(userId);

        // then
        assert responses.size() >= 2;
        assert responses.get(0).getUserId().equals(userId);
    }
}
