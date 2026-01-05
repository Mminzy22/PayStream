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
public class ProductResponse {

    private Long id;
    private String name;
    private int minPersonCount;
    private int maxPersonCount;
    private int price;
    private boolean isAvailable;

    public static ProductResponse of(Product product, boolean isAvailable) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                .price(product.getBasePrice())
                .isAvailable(isAvailable)
                .build();
    }
}
