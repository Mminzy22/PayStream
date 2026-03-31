package com.paystream.order.event;

import java.time.LocalDate;

/** 주문이 DB에 커밋된 뒤 재고 선점(reserve) Kafka 메시지를 보내기 위한 이벤트입니다. */
public record OrderCreatedEvent(
        Long orderId,
        String userId,
        Long productId,
        LocalDate checkInDate,
        LocalDate checkOutDate) {}
