package com.example.inventory.store.dto.response;

import com.example.inventory.product.dto.response.ProductResponse;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.entity.Address;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@ToString
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long id;
    private String hostId;
    private String name;
    private String description;
    private Address address;
    private Category category;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private double rating;
    private int reviewCount;
    private String rules;
    private List<Amenities> amenities;
    private int minPrice;
    private List<ProductResponse> products;

    public static StoreResponse of(Store store, int minPrice) {
        return createBaseBuilder(store)
                .minPrice(minPrice)
                .build();
    }

    public static StoreResponse of(Store store) {
        return createBaseBuilder(store)
                .build();
    }

    public static StoreResponse ofWithProducts(Store store)  {
        List<ProductResponse> product = store.getProducts().stream()
                .map(ProductResponse::of)
                .toList();

        return createBaseBuilder(store)
                .products(product)
                .build();
    }

    private static StoreResponseBuilder createBaseBuilder(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .hostId(store.getHostId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .category(store.getCategory())
                .checkInTime(store.getCheckInTime())
                .checkOutTime(store.getCheckOutTime())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .amenities(store.getAmenities());
    }
}