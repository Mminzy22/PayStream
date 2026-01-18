package com.paystream.payment.controller;

import com.paystream.core.BaseResponse;
import com.paystream.payment.dto.PaymentConfirmDto;
import com.paystream.payment.dto.PaymentRequestDto;
import com.paystream.payment.dto.PaymentResponseDto;
import com.paystream.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/ping")
    public BaseResponse<Map<String, Object>> ping() {
        return BaseResponse.ok(
                Map.of(
                        "service", "payment-service",
                        "status", "UP",
                        "timestamp", LocalDateTime.now()));
    }

    /** 결제 요청 생성 */
    @PostMapping("/request")
    public BaseResponse<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentRequestDto requestDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        PaymentResponseDto response = paymentService.createPayment(requestDto, userId);
        return BaseResponse.created(response);
    }

    /** 결제 승인 처리 */
    @PostMapping("/confirm")
    public BaseResponse<PaymentResponseDto> confirmPayment(
            @Valid @RequestBody PaymentConfirmDto confirmDto) {
        PaymentResponseDto response = paymentService.confirmPayment(confirmDto);
        return BaseResponse.ok(response);
    }

    /** 결제 조회 (ID로) */
    @GetMapping("/{id}")
    public BaseResponse<PaymentResponseDto> getPayment(@PathVariable Long id) {
        PaymentResponseDto response = paymentService.getPayment(id);
        return BaseResponse.ok(response);
    }

    /** 결제 조회 (포트원 ID로) */
    @GetMapping("/imp/{impUid}")
    public BaseResponse<PaymentResponseDto> getPaymentByImpUid(@PathVariable String impUid) {
        PaymentResponseDto response = paymentService.getPaymentByImpUid(impUid);
        return BaseResponse.ok(response);
    }

    /** 결제 조회 (주문 ID로) */
    @GetMapping("/order/{orderId}")
    public BaseResponse<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentResponseDto> responses = paymentService.getPaymentsByOrderId(orderId);
        return BaseResponse.ok(responses);
    }

    /** 결제 취소 */
    @PostMapping("/cancel/{impUid}")
    public BaseResponse<PaymentResponseDto> cancelPayment(
            @PathVariable String impUid,
            @RequestParam(required = false, defaultValue = "고객 요청") String reason) {
        PaymentResponseDto response = paymentService.cancelPayment(impUid, reason);
        return BaseResponse.ok(response);
    }

    /** 사용자별 결제 목록 조회 */
    @GetMapping
    public BaseResponse<List<PaymentResponseDto>> getAllPayments(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<PaymentResponseDto> responses =
                paymentService.getAllPaymentsByUserId(userId, page, size);
        return BaseResponse.ok(responses);
    }

    /** 포트원 설정 정보 조회 */
    @GetMapping("/config")
    public BaseResponse<Map<String, String>> getPortOneConfig() {
        return BaseResponse.ok(
                Map.of(
                        "storeId", paymentService.getPortOneStoreId(),
                        "channelKey", paymentService.getPortOneChannelKey()));
    }

    /** 현재 로그인한 사용자 ID 가져오기 */
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
