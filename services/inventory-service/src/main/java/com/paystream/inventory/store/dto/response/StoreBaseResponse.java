package com.paystream.inventory.store.dto.response;

import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreBaseResponse {

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
    private List<String> amenities;

    public static StoreBaseResponse from(Store store) {
        List<String> responseAmenities =
                Optional.ofNullable(store.getAmenities()).orElseGet(List::of).stream()
                        .map(Amenities::getDisplayName)
                        .toList();

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
                .rules(store.getRule())
                .amenities(responseAmenities)
                .build();
    }
}
