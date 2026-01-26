package com.paystream.inventory.kafka.consumer;

import com.paystream.inventory.inventory.service.StockManagerService;
import com.paystream.inventory.kafka.StockAction;
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

    /**
     * 공통 로직을 담은 템플릿 메서드
     *
     * @param topic 카프카 topic
     * @param event 카프카 메시지
     * @param stockAction 재고관리 비즈니스 로직
     */
    private void executeWithLogging(String topic, InventoryEvent event, StockAction stockAction) {
        log.info(
                "[Kafka Consumer] Topic: {}, ProductId: {}, Period: {} ~ {}",
                topic,
                event.getProductId(),
                event.getCheckInDate(),
                event.getCheckOutDate());

        try {
            long startTime = System.currentTimeMillis();

            // 비즈니스 로직
            stockAction.execute(
                    event.getProductId(), event.getCheckInDate(), event.getCheckOutDate());

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

    /** 주문 토픽으로부터 메시지를 수신하여 처리, 재고 감소 로직 수행 @Param message 수신된 주문 메시지 */
    @KafkaListener(topics = "order.inventory.decrease", groupId = "inventory-group")
    public void consumeOrderDecreaseInventory(InventoryEvent event) {
        executeWithLogging("order.inventory.decrease", event, stockManagerService::decreaseStock);
    }

    /** 주문 토픽으로부터 메시지를 수신하여 처리, 재고 증가 로직 수행 @Param message 수신된 주문 메시지 */
    @KafkaListener(topics = "order.inventory.increase", groupId = "inventory-group")
    public void consumeOrderIncreaseInventory(InventoryEvent event) {
        executeWithLogging("order.inventory.increase", event, stockManagerService::increaseStock);
    }
}
