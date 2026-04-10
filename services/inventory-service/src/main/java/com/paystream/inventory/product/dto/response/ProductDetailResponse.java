package com.paystream.inventory.product.dto.response;

import com.paystream.inventory.inventory.dto.response.DailyInventoryResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.promotion.dto.response.DiscountResult;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse extends ProductResponse {

    private String description;
    private int personAddPrice;
    private List<DailyInventoryResponse> dailyInventories;

    private static ProductDetailResponse.ProductDetailResponseBuilder<?, ?> createBuilder(
            Product product, DiscountResult dr) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .minPersonCount(product.getMinPersonCount())
                .maxPersonCount(product.getMaxPersonCount())
                .personAddPrice(product.getPersonAddPrice())
                .price(
                        PriceInfo.builder()
                                .original(dr.getOriginPrice())
                                .discounted(dr.getDiscountedPrice())
                                .discountRate(dr.getDiscountRate())
                                .hasDiscount(dr.getDiscountRate() > 0)
                                .build());
    }

    public static ProductDetailResponse of(
            Product product, List<String> images, DiscountResult dr) {
        return createBuilder(product, dr).images(images).build();
    }

    public static ProductDetailResponse of(
            Product product,
            List<DailyInventoryResponse> dailyInventories,
            List<String> images,
            DiscountResult dr) {
        return createBuilder(product, dr).dailyInventories(dailyInventories).images(images).build();
    }
}
