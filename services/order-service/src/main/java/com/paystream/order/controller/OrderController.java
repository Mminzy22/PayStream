package com.paystream.order.controller;

import com.paystream.core.BaseResponse;
import com.paystream.order.dto.OrderCreateRequest;
import com.paystream.order.dto.OrderResponse;
import com.paystream.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public BaseResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        OrderResponse response = orderService.createOrder(userId, request);
        return BaseResponse.created(response);
    }

    @GetMapping("/{id:\\d+}")
    public BaseResponse<OrderResponse> getOrder(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        OrderResponse response = orderService.getOrder(userId, id);
        return BaseResponse.ok(response);
    }

    @GetMapping
    public BaseResponse<List<OrderResponse>> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<OrderResponse> responses = orderService.getOrdersByUserId(userId, page, size);
        return BaseResponse.ok(responses);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-Auth-User-Id");
        if (userIdHeader == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다. API Gateway를 통해 요청해야 합니다.");
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID입니다: " + userIdHeader);
        }
    }
}
