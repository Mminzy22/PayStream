package com.paystream.order.dto;

import com.paystream.order.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderResponse {

    private Long id;
    private Long userId;
    private String status;
    private BigDecimal amount;
    private String merchantUid;
    private String name;
    private Long productId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus().name());
        response.setAmount(order.getAmount());
        response.setMerchantUid(order.getMerchantUid());
        response.setName(order.getName());
        response.setProductId(order.getProductId());
        response.setCheckInDate(order.getCheckInDate());
        response.setCheckOutDate(order.getCheckOutDate());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantUid() {
        return merchantUid;
    }

    public void setMerchantUid(String merchantUid) {
        this.merchantUid = merchantUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
