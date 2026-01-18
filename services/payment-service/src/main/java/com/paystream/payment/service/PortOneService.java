package com.paystream.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.payment.dto.PortOnePaymentResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 포트원 V2 REST API 서비스
 *
 * <p>참고: https://developers.portone.io/api/rest-v2/overview?v=v2 - Hostname: api.portone.io - 인증:
 * Authorization: PortOne {API_SECRET}
 */
@Service
public class PortOneService {

    private static final Logger log = LoggerFactory.getLogger(PortOneService.class);
    private static final String BASE_URL = "https://api.portone.io";
    private static final int TIMEOUT_SECONDS = 60;

    private final WebClient webClient;
    private final String apiSecret;
    private final ObjectMapper objectMapper;

    public PortOneService(
            @Value("${portone.secret-key}") String apiSecret,
            @Value("${portone.store-id}") String storeId) {
        this.apiSecret = apiSecret;
        this.objectMapper = new ObjectMapper();

        ExchangeFilterFunction authFilter =
                ExchangeFilterFunction.ofRequestProcessor(
                        clientRequest -> {
                            if (apiSecret != null && !apiSecret.isEmpty()) {
                                String authHeader = "PortOne " + apiSecret.trim();
                                ClientRequest.Builder requestBuilder =
                                        ClientRequest.from(clientRequest);
                                requestBuilder.headers(
                                        headers -> {
                                            headers.remove(HttpHeaders.AUTHORIZATION);
                                            headers.add(HttpHeaders.AUTHORIZATION, authHeader);
                                        });
                                return Mono.just(requestBuilder.build());
                            }
                            return Mono.just(clientRequest);
                        });

        this.webClient =
                WebClient.builder()
                        .baseUrl(BASE_URL)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .filter(authFilter)
                        .build();
    }

    /** 결제 정보 조회 */
    public PortOnePaymentResponse.PaymentData getPayment(String paymentId) {
        try {
            validateApiSecret();

            JsonNode response =
                    webClient
                            .get()
                            .uri("/payments/" + paymentId)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                            .block();

            if (response == null) {
                throw new RuntimeException("포트원 API 응답이 null입니다.");
            }

            return parsePaymentData(response);

        } catch (WebClientResponseException e) {
            handleWebClientResponseException(e, paymentId);
            throw new RuntimeException("포트원 결제 조회 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("포트원 결제 조회 중 오류 발생: paymentId={}, error={}", paymentId, e.getMessage(), e);
            throw new RuntimeException("포트원 결제 조회 실패: " + e.getMessage(), e);
        }
    }

    /** 결제 취소 */
    public void cancelPayment(String paymentId, String reason) {
        try {
            validateApiSecret();

            String idempotencyKey = UUID.randomUUID().toString();

            JsonNode response =
                    webClient
                            .post()
                            .uri("/payments/" + paymentId + "/cancel")
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("Idempotency-Key", "\"" + idempotencyKey + "\"")
                            .bodyValue(java.util.Map.of("reason", reason))
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                            .block();

            if (response == null) {
                throw new RuntimeException("포트원 취소 API 응답이 null입니다.");
            }

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                throw new RuntimeException("포트원 결제 취소 처리 중입니다. 잠시 후 다시 시도해주세요.", e);
            }
            log.error(
                    "포트원 취소 API 호출 중 오류 발생: paymentId={}, status={}, error={}",
                    paymentId,
                    e.getStatusCode(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("포트원 결제 취소 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("포트원 결제 취소 중 오류 발생: paymentId={}, error={}", paymentId, e.getMessage(), e);
            throw new RuntimeException("포트원 결제 취소 실패: " + e.getMessage(), e);
        }
    }

    /** 결제 다건 조회 */
    public List<PortOnePaymentResponse.PaymentData> getPayments(Integer page, Integer size) {
        try {
            validateApiSecret();

            int pageNum = (page != null && page > 0) ? page : 1;
            int pageSize = (size != null && size > 0) ? size : 20;

            String requestBody =
                    String.format("{\"page\":{\"number\":%d,\"size\":%d}}", pageNum, pageSize);
            String encodedRequestBody =
                    java.net.URLEncoder.encode(requestBody, StandardCharsets.UTF_8);
            String requestUrl = "/payments?requestBody=" + encodedRequestBody;

            JsonNode response =
                    webClient
                            .get()
                            .uri(requestUrl)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                            .block();

            if (response == null) {
                throw new RuntimeException("포트원 API 응답이 null입니다.");
            }

            List<PortOnePaymentResponse.PaymentData> paymentList = new ArrayList<>();
            if (response.has("items") && response.get("items").isArray()) {
                for (JsonNode item : response.get("items")) {
                    paymentList.add(parsePaymentData(item));
                }
            }

            return paymentList;

        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error(
                    "포트원 결제 목록 조회 실패: page={}, size={}, status={}, error={}, responseBody={}",
                    page,
                    size,
                    e.getStatusCode(),
                    e.getMessage(),
                    responseBody,
                    e);

            if (e.getStatusCode().value() == 400) {
                try {
                    JsonNode errorBody = objectMapper.readTree(responseBody);
                    if (errorBody.has("type") && errorBody.has("message")) {
                        String errorType = errorBody.get("type").asText();
                        String errorMessage = errorBody.get("message").asText();
                        throw new RuntimeException(
                                "포트원 API 요청 형식 오류 (" + errorType + "): " + errorMessage, e);
                    }
                } catch (Exception parseException) {
                }
            }

            throw new RuntimeException("포트원 결제 목록 조회 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error(
                    "포트원 결제 목록 조회 중 오류 발생: page={}, size={}, error={}",
                    page,
                    size,
                    e.getMessage(),
                    e);
            throw new RuntimeException("포트원 결제 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    /** JsonNode에서 PaymentData로 파싱 */
    private PortOnePaymentResponse.PaymentData parsePaymentData(JsonNode item) {
        PortOnePaymentResponse.PaymentData paymentData = new PortOnePaymentResponse.PaymentData();

        if (item.has("id")) {
            paymentData.setMerchantUid(item.get("id").asText());
        }
        if (item.has("transactionId")) {
            paymentData.setImpUid(item.get("transactionId").asText());
        }
        if (item.has("orderName")) {
            paymentData.setName(item.get("orderName").asText());
        }
        if (item.has("amount")) {
            JsonNode amountNode = item.get("amount");
            if (amountNode.has("total")) {
                paymentData.setAmount(BigDecimal.valueOf(amountNode.get("total").asLong()));
            }
        }
        if (item.has("status")) {
            paymentData.setStatus(item.get("status").asText().toLowerCase());
        } else {
            paymentData.setStatus("paid");
        }
        if (item.has("method")) {
            JsonNode methodNode = item.get("method");
            if (methodNode.isTextual()) {
                paymentData.setPayMethod(methodNode.asText());
            } else if (methodNode.has("type")) {
                paymentData.setPayMethod(methodNode.get("type").asText());
            }
        }
        if (item.has("customer")) {
            JsonNode customerNode = item.get("customer");
            if (customerNode.has("fullName")) {
                paymentData.setBuyerName(customerNode.get("fullName").asText());
            } else if (customerNode.has("name")) {
                paymentData.setBuyerName(customerNode.get("name").asText());
            }
            if (customerNode.has("email")) {
                paymentData.setBuyerEmail(customerNode.get("email").asText());
            }
            if (customerNode.has("phoneNumber")) {
                paymentData.setBuyerTel(customerNode.get("phoneNumber").asText());
            } else if (customerNode.has("tel")) {
                paymentData.setBuyerTel(customerNode.get("tel").asText());
            }
        }
        if (item.has("statusChangedAt")) {
            JsonNode statusChangedAtNode = item.get("statusChangedAt");
            if (statusChangedAtNode.isTextual()) {
                try {
                    java.time.Instant instant =
                            java.time.Instant.parse(statusChangedAtNode.asText());
                    paymentData.setPaidAt(instant.getEpochSecond());
                } catch (Exception e) {
                }
            }
        } else if (item.has("paidAt")) {
            JsonNode paidAtNode = item.get("paidAt");
            if (paidAtNode.isNumber()) {
                paymentData.setPaidAt(paidAtNode.asLong());
            } else if (paidAtNode.isTextual()) {
                try {
                    java.time.Instant instant = java.time.Instant.parse(paidAtNode.asText());
                    paymentData.setPaidAt(instant.getEpochSecond());
                } catch (Exception e) {
                }
            }
        }

        return paymentData;
    }

    private void validateApiSecret() {
        if (apiSecret == null || apiSecret.isEmpty()) {
            throw new RuntimeException("포트원 API Secret이 설정되지 않았습니다.");
        }
    }

    private void handleWebClientResponseException(WebClientResponseException e, String paymentId) {
        int statusCode = e.getStatusCode().value();
        String responseBody = e.getResponseBodyAsString();

        if (statusCode == 401) {
            log.error("포트원 API 인증 실패 (401): paymentId={}", paymentId);
            throw new RuntimeException("포트원 API 인증 실패: API Secret을 확인하세요.", e);
        }
        if (statusCode == 400) {
            log.error("포트원 API 요청 형식 오류 (400): paymentId={}", paymentId);
            try {
                JsonNode errorBody = objectMapper.readTree(responseBody);
                if (errorBody.has("type") && errorBody.has("message")) {
                    String errorType = errorBody.get("type").asText();
                    String errorMessage = errorBody.get("message").asText();
                    throw new RuntimeException(
                            "포트원 API 요청 형식 오류 (" + errorType + "): " + errorMessage, e);
                }
            } catch (Exception parseException) {
            }
            throw new RuntimeException("포트원 API 요청 형식 오류: paymentId 형식이나 엔드포인트를 확인하세요.", e);
        }
        if (statusCode == 404) {
            log.error("포트원 결제 건을 찾을 수 없음 (404): paymentId={}", paymentId);
            throw new RuntimeException("포트원 결제 건을 찾을 수 없습니다. paymentId를 확인하세요.", e);
        }
        log.error(
                "포트원 API 호출 중 오류 발생: paymentId={}, status={}, error={}",
                paymentId,
                e.getStatusCode(),
                e.getMessage(),
                e);
    }
}
