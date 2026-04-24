package com.paystream.inventory.kafka.consumer;

import static com.paystream.core.exception.ExceptionEnum.RESERVATION_EXPIRED;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.service.StockManagerService;
import com.paystream.inventory.kafka.StockAction;
import com.paystream.inventory.kafka.dto.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private static final String RESERVE_KEY = "reserve:prod:";
    private static final String STOCK_RESERVE = "order.inventory.reserve";
    private static final String STOCK_DECREASE = "order.inventory.decrease";
    private static final String STOCK_INCREASE = "order.inventory.increase";
    private final StockManagerService stockManagerService;
    private final RedisTemplate<String, String> redisTemplate;

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
            stockAction.execute(event);

            long endTime = System.currentTimeMillis();

            // 완료 로그 (소요 시간 포함)
            log.info(
                    "[Kafka Consumer] Success: ProductId: {} duration ({}ms)",
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

    /** 주문 토픽으로부터 메시지를 수신, 재고 선점 로직 수행 */
    @KafkaListener(topics = STOCK_RESERVE, groupId = "inventory-group")
    public void consumeOrderReserveInventory(InventoryEvent event) {
        executeWithLogging(
                STOCK_RESERVE,
                event,
                (e) -> {
                    stockManagerService.reserveStock(
                            e.getUserId(),
                            e.getProductId(),
                            e.getCheckInDate(),
                            e.getCheckOutDate());
                });
    }

    /** 주문 토픽으로부터 메시지를 수신하여 처리, 재고 감소 로직 수행 */
    @KafkaListener(topics = STOCK_DECREASE, groupId = "inventory-group")
    public void consumeOrderDecreaseInventory(InventoryEvent event) {
        executeWithLogging(
                STOCK_DECREASE,
                event,
                (e) -> {
                    validateReserve(e);
                    stockManagerService.decreaseStock(
                            e.getProductId(), e.getCheckInDate(), e.getCheckOutDate());
                });
    }

    /** 주문 토픽으로부터 메시지를 수신하여 처리, 재고 증가 로직 수행 */
    @KafkaListener(topics = STOCK_INCREASE, groupId = "inventory-group")
    public void consumeOrderIncreaseInventory(InventoryEvent event) {
        executeWithLogging(
                STOCK_INCREASE,
                event,
                (e) -> {
                    validateReserve(e);
                    stockManagerService.increaseStock(
                            e.getProductId(), e.getCheckInDate(), e.getCheckOutDate());
                });
    }

    // 재고 선점 여부 확인
    private void validateReserve(InventoryEvent e) {
        String reserveKey = RESERVE_KEY + e.getProductId() + ":user:" + e.getUserId();
        Boolean isReserved = redisTemplate.hasKey(reserveKey);

        if (!isReserved) {
            throw new PayStreamException(RESERVATION_EXPIRED);
        }
    }
}
