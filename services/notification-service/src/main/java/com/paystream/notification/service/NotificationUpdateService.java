package com.paystream.notification.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.domain.Notification;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.dto.NotificationUpdateRequest;
import com.paystream.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 알림 업데이트 서비스 */
@Service
@RequiredArgsConstructor
public class NotificationUpdateService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 상태 업데이트 (읽음 처리 등)
     *
     * @param id 알림 ID
     * @param request 업데이트 요청
     * @return 업데이트된 알림 응답
     */
    @Transactional
    public NotificationResponse update(Long id, NotificationUpdateRequest request) {
        Notification notification =
                notificationRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new PayStreamException(ExceptionEnum.NOTIFICATION_NOT_FOUND));

        // 상태 업데이트
        if (request.getStatus() != null) {
            notification.setStatus(request.getStatus());
        }

        return NotificationResponse.of(notification);
    }
}
