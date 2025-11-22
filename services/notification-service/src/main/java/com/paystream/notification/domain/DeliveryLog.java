package com.paystream.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 알림 발송 이력 */
@Entity
@Table(
        name = "delivery_log",
        indexes = {
            @Index(name = "idx_delivery_notification", columnList = "notification_id"),
            @Index(name = "idx_delivery_status", columnList = "status"),
            @Index(name = "idx_delivery_requested_at", columnList = "requested_at")
        })
public class DeliveryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private NotificationStatus status;

    @Column(name = "channel_message_id", length = 128)
    private String channelMessageId; // 외부(FCM/SMTP) 식별자

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getChannelMessageId() {
        return channelMessageId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public void setChannelMessageId(String channelMessageId) {
        this.channelMessageId = channelMessageId;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
