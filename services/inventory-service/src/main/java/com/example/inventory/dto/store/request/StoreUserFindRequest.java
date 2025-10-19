package com.example.inventory.dto.store.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StoreUserFindRequest {

    private String name;
    private String category;
    private String province;
    private String city;
    private List<String> amenities;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

}
