package com.paystream.notification.dto;

import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    // 알림 ID
    private Long id;

    // 수신 사용자 식별자
    private Long userId;

    // 알림 채널
    private NotificationChannel channel;

    // 알림 제목
    private String title;

    // 알림 본문
    private String body;

    // 알림 상태
    private NotificationStatus status;

    // 예약 전송 시각
    private LocalDateTime scheduledAt;

    // 실제 전송 시각
    private LocalDateTime sentAt;

    // 재시도 횟수
    private int retryCount;

    // 템플릿 코드
    private String templateCode;

    // 생성 시각
    private LocalDateTime createdAt;

    // 수정 시각
    private LocalDateTime updatedAt;

    /**
     * Notification 엔티티를 NotificationResponse로 변환
     *
     * @param notification 알림 엔티티
     * @return NotificationResponse
     */
    public static NotificationResponse of(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .body(notification.getBody())
                .status(notification.getStatus())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .templateCode(notification.getTemplateCode())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
