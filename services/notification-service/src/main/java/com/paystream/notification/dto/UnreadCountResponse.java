package com.paystream.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 읽지 않은 알림 개수 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {

    // 사용자 ID
    private Long userId;

    // 읽지 않은 알림 개수
    private long unreadCount;
}
