package com.paystream.inventory.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PriceInfo {
    private long original; // 원가
    private long discounted; // 최종가
    private double discountRate; // 할인율
    private boolean hasDiscount; // 할인 여부
}
