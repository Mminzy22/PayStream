package com.paystream.inventory.kafka;

import java.time.LocalDate;

@FunctionalInterface
public interface StockAction {
    void execute(Long productId, LocalDate checkInDate, LocalDate checkOutDate);
}
