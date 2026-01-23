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

    Long productId;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    String eventType;
}
