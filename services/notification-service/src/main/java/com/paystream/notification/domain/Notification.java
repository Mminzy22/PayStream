package com.paystream.notification.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Notification 엔티티
 * - 하나의 알림 메시지 단위
 */
@Entity
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_status", columnList = "user_id,status"),
                @Index(name = "idx_notification_scheduled_at", columnList = "scheduled_at")
        })
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수신 사용자 식별자
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 채널 (PUSH/EMAIL/SMS)
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private NotificationChannel channel;

    // 제목/본문
    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "body", length = 2000)
    private String body;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private NotificationStatus status = NotificationStatus.PENDING;

    // 예약/전송 시각
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // 재시도 횟수
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    // 템플릿 참조 (선택)
    @Column(name = "template_code", length = 100)
    private String templateCode;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public NotificationChannel getChannel() { return channel; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public NotificationStatus getStatus() { return status; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public int getRetryCount() { return retryCount; }
    public String getTemplateCode() { return templateCode; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }
    public void setTitle(String title) { this.title = title; }
    public void setBody(String body) { this.body = body; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
}


