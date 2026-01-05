package com.paystream.inventory.store.dto.request;

import com.paystream.inventory.store.entity.Amenities;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private int basePersonCount;
    private List<Amenities> amenities;
    private String rule;
}
