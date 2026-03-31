package com.paystream.order.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.order.dto.OrderCreateRequest;
import com.paystream.order.dto.OrderResponse;
import com.paystream.order.entity.Order;
import com.paystream.order.entity.OrderStatus;
import com.paystream.order.event.OrderCreatedEvent;
import com.paystream.order.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new PayStreamException(ExceptionEnum.INVALID_DATE_RANGE);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(request.getAmount());
        order.setName(request.getName());
        order.setProductId(request.getProductId());
        order.setCheckInDate(request.getCheckInDate());
        order.setCheckOutDate(request.getCheckOutDate());

        Order saved = orderRepository.save(order);
        saved.setMerchantUid("ORD-" + String.format("%012d", saved.getId()));
        orderRepository.save(saved);

        eventPublisher.publishEvent(
                new OrderCreatedEvent(
                        saved.getId(),
                        String.valueOf(userId),
                        saved.getProductId(),
                        saved.getCheckInDate(),
                        saved.getCheckOutDate()));

        return OrderResponse.from(saved);
    }

    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(userId)) {
            throw new PayStreamException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId, Integer page, Integer size) {
        List<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 20;
        int start = pageNum * pageSize;
        int end = Math.min(start + pageSize, orders.size());

        List<Order> paged = start < orders.size() ? orders.subList(start, end) : new ArrayList<>();

        return paged.stream().map(OrderResponse::from).collect(Collectors.toList());
    }
}
