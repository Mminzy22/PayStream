package com.paystream.inventory.promotion.dto.request;

import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.PromotionStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionFindRequest {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // 행사상태

    public Promotion toEntity() {
        return Promotion.builder()
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .status(PromotionStatus.valueOf(status))
                .build();
    }
}
