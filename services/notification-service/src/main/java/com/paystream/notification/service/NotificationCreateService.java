package com.paystream.notification.service;

import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.dto.NotificationRequest;
import com.paystream.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 알림 생성 서비스 */
@Service
@RequiredArgsConstructor
public class NotificationCreateService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     *
     * @param request 알림 생성 요청
     * @return 생성된 알림 ID
     */
    @Transactional
    public Long create(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setChannel(request.getChannel());
        notification.setTitle(request.getTitle());
        notification.setBody(request.getBody());
        notification.setScheduledAt(request.getScheduledAt());
        notification.setTemplateCode(request.getTemplateCode());
        notification.setStatus(NotificationStatus.PENDING);

        // 예약 시간이 없으면 즉시 전송 준비 상태로 설정
        if (request.getScheduledAt() == null
                || request.getScheduledAt().isBefore(LocalDateTime.now())) {
            // 즉시 전송 가능한 상태로 설정 (실제 전송은 별도 스케줄러에서 처리)
            notification.setScheduledAt(LocalDateTime.now());
        }

        return notificationRepository.save(notification).getId();
    }
}
