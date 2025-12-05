package com.paystream.notification.dto;

import com.paystream.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 템플릿 생성/수정 요청 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateRequest {

    // 템플릿 식별 코드
    @NotBlank(message = "템플릿 코드는 필수입니다")
    private String code;

    // 알림 채널
    @NotNull(message = "알림 채널은 필수입니다")
    private NotificationChannel channel;

    // 로케일 (예: ko_KR)
    private String locale;

    // 제목 템플릿
    private String titleTemplate;

    // 본문 템플릿
    private String bodyTemplate;
}
