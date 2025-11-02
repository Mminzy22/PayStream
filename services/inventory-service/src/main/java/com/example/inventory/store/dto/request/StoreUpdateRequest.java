package com.example.inventory.store.dto.request;

import com.example.inventory.store.entity.Amenities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    private String hostId;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private int basePersonCount;
    private List<Amenities> amenities;
    private String rule;

}
