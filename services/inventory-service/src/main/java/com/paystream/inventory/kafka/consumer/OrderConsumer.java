package com.paystream.inventory.kafka.consumer;

import com.paystream.inventory.inventory.service.StockManagerService;
import com.paystream.inventory.kafka.dto.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final StockManagerService stockManagerService;

    /** 주문 토픽으로부터 메시지를 수신하여 처리, 재고 감소 로직 수행 @Param message 수신된 주문 메시지 */
    @KafkaListener(topics = "inventory.stock.events", groupId = "inventory-group")
    public void consumeOrderDecreaseInventory(InventoryEvent event) {
        log.info(
                "[Kafka Consumer] Topic: inventory.stock.events, ProductId: {}, Period: {} ~ {}",
                event.getProductId(),
                event.getCheckInDate(),
                event.getCheckOutDate());

        try {
            long startTime = System.currentTimeMillis();

            // 비즈니스 로직
            if ("DECREASE".equals(event.getEventType())) {
                stockManagerService.decreaseStock(
                        event.getProductId(), event.getCheckInDate(), event.getCheckOutDate());
            } else {
                stockManagerService.increaseStock(
                        event.getProductId(), event.getCheckInDate(), event.getCheckOutDate());
            }

            long endTime = System.currentTimeMillis();

            // 완료 로그 (소요 시간 포함)
            log.info(
                    "[Kafka Consumer] Success: ProductId: {} stock decreased. ({}ms)",
                    event.getProductId(),
                    (endTime - startTime));
        } catch (Exception e) {
            log.error(
                    "[Kafka Consumer] Failed to decrease stock for ProductId: {}. Error: {}",
                    event.getProductId(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
