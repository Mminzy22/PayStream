package com.paystream.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 사용자 디바이스 푸시 토큰 관리 */
@Entity
@Table(
        name = "recipient_token",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_device_token",
                    columnNames = {"device_token"})
        },
        indexes = {@Index(name = "idx_recipient_token_user", columnList = "user_id")})
public class RecipientToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider; // 예: FCM

    @Column(name = "device_token", nullable = false, length = 512)
    private String deviceToken;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
