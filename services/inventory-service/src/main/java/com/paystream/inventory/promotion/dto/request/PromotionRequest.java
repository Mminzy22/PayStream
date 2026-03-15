package com.paystream.inventory.promotion.dto.request;

import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    private String title;
    private String targetType;
    private Long targetId;
    private String discountType;
    private int discountValue;
    private LocalDate startDate;
    private LocalDate endDate;

    public Promotion toEntity() {
        return Promotion.builder()
                .title(title)
                .targetType(TargetType.valueOf(targetType))
                .targetId(targetId)
                .discountType(DiscountType.valueOf(discountType))
                .discountValue(discountValue)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
