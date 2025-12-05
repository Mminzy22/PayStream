package com.paystream.notification.dto;

import com.paystream.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 생성 요청 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    // 수신 사용자 식별자
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    // 알림 채널
    @NotNull(message = "알림 채널은 필수입니다")
    private NotificationChannel channel;

    // 알림 제목
    @NotBlank(message = "알림 제목은 필수입니다")
    private String title;

    // 알림 본문
    @NotBlank(message = "알림 본문은 필수입니다")
    private String body;

    // 예약 전송 시각 (선택, null이면 즉시 전송)
    private LocalDateTime scheduledAt;

    // 템플릿 코드 (선택)
    private String templateCode;
}
