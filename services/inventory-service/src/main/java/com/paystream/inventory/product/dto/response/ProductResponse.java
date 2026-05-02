package com.paystream.inventory.product.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paystream.inventory.product.entity.Product;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private int minPersonCount;
    private int maxPersonCount;

    // 계층형 구조화: 내부 객체로 선언
    private PriceInfo price;

    @JsonProperty("available") // Jackson에서 직렬화시 is를 빼버린다. 따라서 Redis에서 매핑할때를 위해 명시적으로 선언한다.
    private boolean isAvailable;

    private List<String> images;

    public static ProductResponse of(
            Product product,
            boolean isAvailable,
            List<String> images,
            long originalPrice,
            long finalPrice,
            double rate) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                // 내부 객체 빌더로 생성
                .price(
                        PriceInfo.builder()
                                .original(originalPrice)
                                .discounted(finalPrice)
                                .discountRate(rate)
                                .hasDiscount(rate > 0.0)
                                .build())
                .isAvailable(isAvailable)
                .images(images)
                .build();
    }

    public static ProductResponse from(Product product) {
        return ProductResponse.builder().id(product.getId()).name(product.getName()).build();
    }
}
