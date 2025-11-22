package com.paystream.notification.repository;

import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByCode(String code);

    Optional<NotificationTemplate> findByCodeAndChannelAndLocale(
            String code, NotificationChannel channel, String locale);
}
