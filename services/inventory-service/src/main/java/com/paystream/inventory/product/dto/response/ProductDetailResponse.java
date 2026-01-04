package com.paystream.inventory.product.dto.response;

import com.paystream.inventory.inventory.dto.response.DailyInventoryResponse;
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
public class ProductDetailResponse {

    private Long productId;
    private String name;
    private String description;
    private int minPersonCount;
    private int maxPersonCount;
    private int basePrice;
    private int personAddPrice;
    private List<DailyInventoryResponse> dailyInventories;
    private List<String> images;

    private static ProductDetailResponse.ProductDetailResponseBuilder createBuilder(
            Product product) {
        return ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                .basePrice(product.getBasePrice())
                .personAddPrice(product.getPersonAddPrice())
                .images(product.getPhotos().stream().map(Photo::getImagePath).toList());
    }

    public static ProductDetailResponse of(Product product) {
        return createBuilder(product).build();
    }

    public static ProductDetailResponse of(
            Product product, List<DailyInventoryResponse> dailyInventories) {
        return createBuilder(product).dailyInventories(dailyInventories).build();
    }
}
