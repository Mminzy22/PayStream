package com.paystream.notification.domain;

import jakarta.persistence.*;

/** 알림 메시지 템플릿 */
@Entity
@Table(
        name = "notification_template",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_template_code",
                    columnNames = {"code"})
        },
        indexes = {@Index(name = "idx_template_channel_locale", columnList = "channel,locale")})
public class NotificationTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 100)
    private String code; // 템플릿 식별 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private NotificationChannel channel;

    @Column(name = "locale", length = 16)
    private String locale; // 예: ko_KR

    @Column(name = "title_template", length = 200)
    private String titleTemplate;

    @Column(name = "body_template", length = 4000)
    private String bodyTemplate;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getLocale() {
        return locale;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }
}
