package com.paystream.order.kafka.dto;

import java.time.LocalDate;

/** inventory-service {@code InventoryEvent}와 동일한 JSON 스키마로 직렬화합니다. */
public class InventoryReservePayload {

    private String userId;
    private Long productId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public InventoryReservePayload() {}

    public InventoryReservePayload(
            String userId, Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        this.userId = userId;
        this.productId = productId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
