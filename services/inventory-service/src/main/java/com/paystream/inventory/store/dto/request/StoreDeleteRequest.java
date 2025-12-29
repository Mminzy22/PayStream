package com.paystream.inventory.store.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreDeleteRequest {

    @NotNull(message = "storeIds는 반드시 입력되어야 합니다.")
    private List<Long> storeIds;
}
