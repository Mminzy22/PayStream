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

    public static ProductResponse of(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minPersonCount(product.getMinCapacity())
                .maxPersonCount(product.getMaxCapacity())
                .price(product.getBasePrice())
                .build();
    }
}
