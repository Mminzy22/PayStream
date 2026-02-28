package com.paystream.inventory.kafka.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

    private String userId;
    private Long productId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
