package com.paystream.notification.dto;

import com.paystream.notification.domain.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 알림 업데이트 요청 DTO (읽음 처리 등) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateRequest {

    // 알림 상태 변경 (예: READ로 변경하여 읽음 처리)
    private NotificationStatus status;
}
