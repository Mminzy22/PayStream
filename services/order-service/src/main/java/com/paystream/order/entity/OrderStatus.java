package com.paystream.order.entity;

/** 주문 상태 */
public enum OrderStatus {
    /** 주문 생성됨, 결제 대기 */
    PENDING,
    /** 결제 완료 */
    PAID,
    /** 주문 취소 */
    CANCELLED
}
