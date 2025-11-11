package com.example.inventory.store.dto.request;

import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreListFindRequest {

    private String name;
    private String province;
    private String city;
    private Category category;
    private List<Amenities> amenities;

    @Min(2)
    private int personCount;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;
}
