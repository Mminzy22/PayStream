package com.paystream.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderCreateRequest {

    @NotNull(message = "주문 금액은 필수입니다")
    @Min(value = 100, message = "주문 금액은 최소 100원 이상이어야 합니다")
    private BigDecimal amount;

    @NotBlank(message = "주문명은 필수입니다")
    private String name;

    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    @NotNull(message = "체크인 날짜는 필수입니다")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 필수입니다")
    private LocalDate checkOutDate;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
