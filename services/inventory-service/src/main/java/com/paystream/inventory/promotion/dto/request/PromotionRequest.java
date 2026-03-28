package com.paystream.inventory.promotion.dto.request;

import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "생성자ID는 필수 입니다.")
    private String createUserId;

    @NotBlank(message = "제목은 필수 항목입니다.")
    private String title;

    @NotBlank(message = "적용 타입(STORE, PRODUCT)은 필수 항목입니다.")
    private String targetType;

    // TargetType에 따라 리스트 선택사항이 가게 또는 상품으로 변경되며
    // 리스트에서 적용하고자 하는 항목을 선택하여 TargetId를 넘겨줘야 한다.
    @NotNull(message = "적용 대상 아이디는 필수 항목입니다.")
    private Long targetId;

    @NotBlank(message = "할인 방식(PERCENTAGE, FIXED_AMOUNT)은 필수 항목입니다.")
    private String discountType;

    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    private int discountValue;

    @NotNull(message = "시작일은 필수 항목입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수 항목입니다.")
    private LocalDate endDate;

    public Promotion toEntity() {
        return Promotion.builder()
                .createUserId(createUserId)
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
