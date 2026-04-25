package com.paystream.inventory.promotion.dto.response;

import com.paystream.inventory.promotion.entity.Promotion;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private String title;
    private String targetType; // STORE, PRODUCT
    private Long targetId; // 가게ID 또는 방ID
    private String discountType; // PERCENT / FIXED_AMOUNT
    private int discountValue; // 10% 또는 5000원
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public static PromotionResponse of(Promotion promotion) {
        return PromotionResponse.builder()
                .title(promotion.getTitle())
                .targetType(promotion.getTargetType().name())
                .targetId(promotion.getTargetId())
                .discountType(promotion.getDiscountType().name())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus().name())
                .build();
    }
}
