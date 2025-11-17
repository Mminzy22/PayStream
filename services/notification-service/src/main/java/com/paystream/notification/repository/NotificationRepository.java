package com.paystream.notification.repository;

import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification 저장소
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자별 최신 알림 조회
    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    // 전송 대기(예약) 큐 조회
    List<Notification> findByStatusAndScheduledAtBefore(NotificationStatus status, LocalDateTime scheduledAt);
}




