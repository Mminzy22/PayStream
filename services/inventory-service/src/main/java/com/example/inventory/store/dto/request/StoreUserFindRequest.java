package com.example.inventory.store.dto.request;

import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StoreUserFindRequest {

    private String name;
    private String province;
    private String city;
    private Category category;
    private List<Amenities> amenities;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

}
