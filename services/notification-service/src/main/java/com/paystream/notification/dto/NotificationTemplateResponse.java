package com.paystream.notification.dto;

import com.paystream.notification.domain.NotificationChannel;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 템플릿 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponse {

    // 템플릿 ID
    private Long id;

    // 템플릿 식별 코드
    private String code;

    // 알림 채널
    private NotificationChannel channel;

    // 로케일
    private String locale;

    // 제목 템플릿
    private String titleTemplate;

    // 본문 템플릿
    private String bodyTemplate;

    // 생성 시각
    private LocalDateTime createdAt;

    // 수정 시각
    private LocalDateTime updatedAt;
}
