package com.paystream.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.core.BaseResponse;
import com.paystream.payment.filter.CachedBodyHttpServletRequest;
import com.paystream.payment.service.PaymentService;
import com.paystream.payment.service.WebhookVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 포트원 웹훅 콜백 컨트롤러 */
@RestController
@RequestMapping("/payments/webhook")
public class PortOneWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PortOneWebhookController.class);
    private final PaymentService paymentService;
    private final WebhookVerificationService webhookVerificationService;
    private final ObjectMapper objectMapper;

    public PortOneWebhookController(
            PaymentService paymentService, WebhookVerificationService webhookVerificationService) {
        this.paymentService = paymentService;
        this.webhookVerificationService = webhookVerificationService;
        this.objectMapper = new ObjectMapper();
    }

    /** 포트원 웹훅 콜백 처리 */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Map<String, String>> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature) {

        try {
            String body;
            if (request instanceof CachedBodyHttpServletRequest) {
                body = ((CachedBodyHttpServletRequest) request).getCachedBody();
            } else {
                log.error("웹훅 요청이 CachedBodyHttpServletRequest로 래핑되지 않았습니다.");
                return BaseResponse.ok(
                        Map.of(
                                "message",
                                "Internal server error: request wrapper not found.",
                                "status",
                                "error"));
            }

            String webhookSecret = paymentService.getWebhookSecret();
            if (!webhookVerificationService.verifyWebhook(
                    body, webhookId, webhookTimestamp, webhookSignature, webhookSecret)) {
                return BaseResponse.ok(
                        Map.of(
                                "message",
                                "Webhook signature verification failed.",
                                "status",
                                "error"));
            }

            JsonNode webhookData = objectMapper.readTree(body);
            String eventType =
                    webhookData.has("type") ? webhookData.get("type").asText() : "unknown";
            String paymentId = null;

            if (webhookData.has("data") && webhookData.get("data").has("paymentId")) {
                paymentId = webhookData.get("data").get("paymentId").asText();
            }

            if (paymentId != null && eventType != null && eventType.startsWith("Transaction.")) {
                paymentService.syncPaymentFromWebhook(paymentId);
                log.info("웹훅 처리 완료: eventType={}, paymentId={}", eventType, paymentId);
            }

            return BaseResponse.ok(
                    Map.of("message", "Webhook processed successfully.", "status", "ok"));

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", e.getMessage(), e);
            return BaseResponse.ok(
                    Map.of(
                            "message",
                            "Error processing webhook: " + e.getMessage(),
                            "status",
                            "error"));
        }
    }
}
