package com.paystream.inventory.product.dto.request;

import com.paystream.inventory.product.entity.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "가게는 필수 입력입니다.")
    private Long storeId;

    @NotBlank(message = "이름은 필수 입력입니다.")
    private String name;

    @NotBlank(message = "설명은 반드시 입력해야합니다.")
    private String description;

    @Min(value = 1000, message = "기본 가격은 1000원 이상부터 입니다.")
    private int basePrice;

    @Min(value = 1000, message = "추가금액은 1000원 이상부터 입니다.")
    private int personAddPrice;

    @Min(value = 2, message = "최소인원은 2명 이상이어야 합니다.")
    private int minPersonCount;

    @Min(value = 2, message = "최대인원은 2명 이상이어야 합니다.")
    private int maxPersonCount;

    @Min(value = 1, message = "재고는 최소 1개 이상이어야 합니다.")
    private int stock; // 초기 재고 생산용

    public Product toEntity() {
        return Product.builder()
                .name(this.name)
                .description(this.description)
                .minPersonCount(this.minPersonCount)
                .maxPersonCount(this.maxPersonCount)
                .basePrice(this.basePrice)
                .personAddPrice(this.personAddPrice)
                .baseStock(this.stock)
                .build();
    }
}
