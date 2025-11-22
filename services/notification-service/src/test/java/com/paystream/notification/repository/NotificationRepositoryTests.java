package com.paystream.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class NotificationRepositoryTests {

    @Autowired private NotificationRepository notificationRepository;

    @Test
    @DisplayName("전송 대기 알림 조회")
    void findPendingBeforeNow() {
        Notification n = new Notification();
        n.setUserId(1L);
        n.setChannel(NotificationChannel.PUSH);
        n.setTitle("테스트");
        n.setBody("본문");
        n.setStatus(NotificationStatus.PENDING);
        n.setScheduledAt(LocalDateTime.now().minusMinutes(5));
        notificationRepository.save(n);

        List<Notification> result =
                notificationRepository.findByStatusAndScheduledAtBefore(
                        NotificationStatus.PENDING, LocalDateTime.now());

        assertThat(result).isNotEmpty();
    }
}
