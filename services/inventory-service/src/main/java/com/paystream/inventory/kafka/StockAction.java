package com.paystream.inventory.kafka;

import com.paystream.inventory.kafka.dto.InventoryEvent;

@FunctionalInterface
public interface StockAction {
    void execute(InventoryEvent event);
}
