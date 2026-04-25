package com.paystream.inventory.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiscountResult {

    private final long originPrice; // 원가
    private final long discountedPrice; // 최종가
    private final int discountRate; // 할인율 (0~100)
    private final long discountAmount; // 할인된 금액 (원가 - 최종가)

    public static DiscountResult noDiscount(long originPrice) {
        return new DiscountResult(originPrice, originPrice, 0, 0);
    }
}
