package com.paystream.inventory.inventory.dto.response;

import com.paystream.inventory.inventory.entity.DailyInventory;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyInventoryResponse {

    private String inventoryId;
    private LocalDate date;
    private int stockAvailable;

    public static DailyInventoryResponse of(DailyInventory dailyInventory) {
        return DailyInventoryResponse.builder()
                .inventoryId(dailyInventory.getId())
                .date(dailyInventory.getDate())
                .stockAvailable(dailyInventory.getStockAvailable())
                .build();
    }
}
