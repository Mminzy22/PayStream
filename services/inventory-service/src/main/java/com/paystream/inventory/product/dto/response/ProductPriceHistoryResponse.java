package com.paystream.inventory.product.dto.response;

import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.Promotion;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceHistoryResponse {

    private LocalDate startDate; // 시작일
    private LocalDate endDate; // 종료일
    private long originPrice; // 원가
    private long discountedPrice; // 최종가
    private double discountRate; // 할인율

    public static ProductPriceHistoryResponse of(Promotion promo, DiscountResult result) {
        return ProductPriceHistoryResponse.builder()
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .originPrice(result.getOriginPrice())
                .discountedPrice(result.getDiscountedPrice())
                .discountRate(result.getDiscountRate())
                .build();
    }
}
