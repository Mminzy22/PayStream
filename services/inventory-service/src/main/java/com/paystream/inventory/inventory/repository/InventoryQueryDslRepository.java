package com.paystream.inventory.inventory.repository;

import java.time.LocalDate;

public interface InventoryQueryDslRepository {

    long inventoriesDecreaseBulk(Long productId, LocalDate checkInDate, LocalDate checkOutDate);

    long inventoriesIncreaseBulk(Long productId, LocalDate checkInDate, LocalDate checkOutDate);
}
