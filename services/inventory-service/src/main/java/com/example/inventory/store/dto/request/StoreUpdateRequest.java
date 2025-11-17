package com.example.inventory.store.dto.request;

import com.example.inventory.store.entity.Amenities;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "hostId는 반드시 입력되어야 합니다.")
    private String hostId;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private int basePersonCount;
    private List<Amenities> amenities;
    private String rule;
}
