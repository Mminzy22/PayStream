package com.paystream.inventory.product.dto.response;

import com.paystream.inventory.inventory.dto.response.DailyInventoryResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.promotion.dto.response.DiscountResult;
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
    private int personAddPrice;
    private PriceInfo price;
    private List<DailyInventoryResponse> dailyInventories;
    private List<String> images;

    private static ProductDetailResponse.ProductDetailResponseBuilder createBuilder(
            Product product, DiscountResult dr) {
        return ProductDetailResponse.builder()
                .productId(product.getId())
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
