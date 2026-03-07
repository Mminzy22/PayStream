package com.paystream.notification.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.domain.Notification;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.repository.NotificationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 알림 조회 서비스 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NotificationFindService {

    private final NotificationRepository notificationRepository;

    /**
     * ID로 알림 조회
     *
     * @param id 알림 ID
     * @return 알림 응답
     */
    public NotificationResponse findById(Long id) {
        Notification notification =
                notificationRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new PayStreamException(ExceptionEnum.NOTIFICATION_NOT_FOUND));

        return NotificationResponse.of(notification);
    }

    /**
     * 사용자별 최신 알림 목록 조회
     *
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    public List<NotificationResponse> findByUserId(Long userId) {
        List<Notification> notifications =
                notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(NotificationResponse::of).collect(Collectors.toList());
    }
}
