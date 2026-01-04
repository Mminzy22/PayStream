package com.paystream.inventory.product.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paystream.inventory.product.entity.Photo;
import com.paystream.inventory.product.entity.Product;
import java.util.List;
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

    @JsonProperty("available") // Jackson에서 직렬화시 is를 빼버린다. 따라서 Redis에서 매핑할때를 위해 명시적으로 선언한다.
    private boolean isAvailable;

    private List<String> images;

    public static ProductResponse of(Product product, boolean isAvailable) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                .price(product.getBasePrice())
                .isAvailable(isAvailable)
                .images(product.getPhotos().stream().map(Photo::getImagePath).toList())
                .build();
    }
}
