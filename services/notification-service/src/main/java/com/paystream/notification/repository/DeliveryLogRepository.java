package com.paystream.notification.repository;

import com.paystream.notification.domain.DeliveryLog;
import com.paystream.notification.domain.NotificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    List<DeliveryLog> findByNotificationId(Long notificationId);

    List<DeliveryLog> findByStatusAndRequestedAtBetween(
            NotificationStatus status, LocalDateTime from, LocalDateTime to);
}
