package com.example.inventory.dto.response;

import com.example.inventory.entity.Amenities;
import com.example.inventory.entity.product.Photo;
import com.example.inventory.entity.product.Product;
import com.example.inventory.entity.store.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class StoreResponse {

    // 서버에서 생성된 고유 ID
    private UUID storeId;

    private String hostId;
    private String name;
    private String description;
    private String address;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    private Set<Amenities> amenities;
    private List<String> photos;
    private String rules;

    private Double rating;
    private Integer reviewCount;

    @Builder
    private StoreResponse(UUID storeId, String hostId, String name, String description, String address, String category, LocalDateTime checkInTime, LocalDateTime checkOutTime, Set<Amenities> amenities, List<String> photos, String rules, Double rating, Integer reviewCount) {
        this.storeId = storeId;
        this.hostId = hostId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.category = category;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.amenities = amenities;
        this.photos = photos;
        this.rules = rules;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public static StoreResponse of(Store store) {
        // 가게에서 첫번째 상품의 사진을 조회
        List<String> photoUrls = store.getProducts().stream()
                .filter(product -> !product.getPhotos().isEmpty())
                .findFirst()
                .map(Product::getPhotos)
                .map(photos -> photos.stream().map(Photo::getUrl).toList())
                .orElse(Collections.emptyList());

        return StoreResponse.builder()
                .storeId(store.getId())
                .hostId(store.getHostId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .category(store.getCategory().getName())
                .checkInTime(store.getCheckInTime())
                .checkOutTime(store.getCheckOutTime())
                .amenities(store.getAmenities())
                .photos(photoUrls)
                .rules(store.getRules())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .build();
    }

}
