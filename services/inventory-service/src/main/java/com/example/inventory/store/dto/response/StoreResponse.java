package com.example.inventory.store.dto.response;

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

    public static StoreResponse of(Store store, int minPrice) {
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
                .amenities(store.getAmenities())
                .minPrice(minPrice)
                .build();
    }

}
