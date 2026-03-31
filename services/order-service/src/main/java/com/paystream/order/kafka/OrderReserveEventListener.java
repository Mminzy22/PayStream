package com.paystream.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.order.event.OrderCreatedEvent;
import com.paystream.order.kafka.dto.InventoryReservePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReserveEventListener {

    public static final String TOPIC_ORDER_INVENTORY_RESERVE = "order.inventory.reserve";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            InventoryReservePayload payload =
                    new InventoryReservePayload(
                            event.userId(),
                            event.productId(),
                            event.checkInDate(),
                            event.checkOutDate());
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(
                    TOPIC_ORDER_INVENTORY_RESERVE, String.valueOf(event.orderId()), json);
            log.debug(
                    "Kafka reserve published: orderId={}, topic={}",
                    event.orderId(),
                    TOPIC_ORDER_INVENTORY_RESERVE);
        } catch (Exception e) {
            log.error(
                    "Kafka reserve publish failed: orderId={}, error={}",
                    event.orderId(),
                    e.getMessage(),
                    e);
            throw new PayStreamException(ExceptionEnum.ORDER_EVENT_PUBLISH_FAILED);
        }
    }
}
