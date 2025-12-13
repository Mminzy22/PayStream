package com.paystream.inventory.product.dto.request;

import com.paystream.inventory.product.entity.Product;
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

    private String description;

    private int minCapacity;
    private int maxCapacity;
    private int basePrice;
    private int personAddPrice;
    private int stock; // 초기 재고 생산용

    //    private List<String> photos; // URL 목록
    //    private String thumbnail;    // 대표 사진 URL

    public Product toEntity() {
        return Product.builder()
                .name(this.name)
                .description(this.description)
                .minCapacity(this.minCapacity)
                .maxCapacity(this.maxCapacity)
                .basePrice(this.basePrice)
                .personAddPrice(this.personAddPrice)
                .build();
    }
}
