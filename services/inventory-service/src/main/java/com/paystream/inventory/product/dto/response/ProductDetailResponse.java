package com.paystream.inventory.product.dto.response;

import com.paystream.inventory.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;
    private String name;
    private String description;
    private int minPersonCount;
    private int maxPersonCount;
    private int basePrice;
    private int personAddPrice;

    public static ProductDetailResponse of(Product product) {
        return ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                .basePrice(product.getBasePrice())
                .personAddPrice(product.getPersonAddPrice())
                .build();
    }
}
