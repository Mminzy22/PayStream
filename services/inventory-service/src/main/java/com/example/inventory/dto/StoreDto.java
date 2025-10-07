package com.example.inventory.dto;

import com.example.inventory.entity.Amenities;
import com.example.inventory.entity.Category;
import com.example.inventory.entity.product.Photo;
import com.example.inventory.entity.product.Product;
import com.example.inventory.entity.store.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StoreDto {

    // 이 클래스는 네임스페이스 역할만 하므로 객체 생성을 막습니다.
    private StoreDto() {}

    /**
     * 가게 생성을 위한 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "호스트 ID는 필수입니다.")
        private String hostId;

        @NotBlank(message = "가게 이름은 필수입니다.")
        private String name;

        private String description;

        @NotBlank(message = "주소는 필수입니다.")
        private String address;

        @NotBlank(message = "카테고리는 필수입니다.")
        private String category;

        @NotNull(message = "체크인 시간은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime checkInTime;

        @NotNull(message = "체크아웃 시간은 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime checkOutTime;

        // 편의시설은 중복이 없으므로 Set으로 관리
        private Set<String> amenities;

        @NotEmpty(message = "사진은 최소 1장 이상 등록해야 합니다.")
        private List<String> photos;

        private String rules; // 이용 규칙

        public Store toEntity() {
            return Store.builder()
                    .hostId(this.hostId)
                    .name(this.name)
                    .description(this.description)
                    .address(this.address)
                    .category(Category.valueOf(this.category))
                    .checkInTime(this.checkInTime)
                    .checkOutTime(this.checkOutTime)
                    // Set<String>을 Set<Amenities> Enum으로 변환
                    .amenities(
                            this.amenities.stream()
                                    .map(Amenities::valueOf) // 문자열을 Enum 상수로 변환
                                    .collect(Collectors.toSet())
                    )
                    .rules(this.rules)
                    .build();
        }
    }

    /**
     * 가게 정보 조회를 위한 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Response {

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

        public static Response from(Store store) {
            // 가게에서 첫번째 상품의 사진을 조회
            List<String> photoUrls = store.getProducts().stream()
                    .filter(product -> !product.getPhotos().isEmpty())
                    .findFirst()
                    .map(Product::getPhotos)
                    .map(photos -> photos.stream().map(Photo::getUrl).toList())
                    .orElse(Collections.emptyList());

            return Response.builder()
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

}
