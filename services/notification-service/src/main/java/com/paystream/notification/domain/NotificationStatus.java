package com.paystream.notification.domain;

/**
 * 알림 처리 상태
 */
public enum NotificationStatus {
    PENDING,   // 대기
    SENDING,   // 전송 중
    SENT,      // 전송 완료
    FAILED     // 전송 실패
}




