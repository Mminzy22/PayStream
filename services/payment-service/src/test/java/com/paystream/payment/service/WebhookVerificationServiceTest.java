package com.paystream.payment.service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookVerificationServiceTest {

    @InjectMocks private WebhookVerificationService webhookVerificationService;

    private String webhookSecret;
    private String webhookId;
    private String webhookTimestamp;
    private String body;

    @BeforeEach
    void setUp() {
        webhookSecret = "test-secret-key-12345";
        webhookId = "evt_1234567890";
        webhookTimestamp = String.valueOf(Instant.now().getEpochSecond());
        body = "{\"type\":\"Transaction.Payment\",\"data\":{\"paymentId\":\"imp_123\"}}";
    }

    @Test
    void verifyWebhook_validSignature_shouldReturnTrue() throws Exception {
        // given
        String signedPayload = webhookId + "." + webhookTimestamp + "." + body;
        String expectedSignature = calculateSignature(signedPayload, webhookSecret);
        String webhookSignature = "v1," + expectedSignature;

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, webhookTimestamp, webhookSignature, webhookSecret);

        // then
        assertTrue(result);
    }

    @Test
    void verifyWebhook_invalidSignature_shouldReturnFalse() {
        // given
        String invalidSignature = "v1,invalid_signature_here";

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, webhookTimestamp, invalidSignature, webhookSecret);

        // then
        assertFalse(result);
    }

    @Test
    void verifyWebhook_signatureWithoutPrefix_shouldReturnTrue() throws Exception {
        // given
        String signedPayload = webhookId + "." + webhookTimestamp + "." + body;
        String expectedSignature = calculateSignature(signedPayload, webhookSecret);

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, webhookTimestamp, expectedSignature, webhookSecret);

        // then
        assertTrue(result);
    }

    @Test
    void verifyWebhook_futureTimestamp_shouldReturnFalse() {
        // given
        String futureTimestamp = String.valueOf(Instant.now().getEpochSecond() + 400); // 6분 40초 후
        String signedPayload = webhookId + "." + futureTimestamp + "." + body;
        String signature;
        try {
            signature = calculateSignature(signedPayload, webhookSecret);
        } catch (Exception e) {
            fail("서명 계산 실패: " + e.getMessage());
            return;
        }

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, futureTimestamp, "v1," + signature, webhookSecret);

        // then
        assertFalse(result);
    }

    @Test
    void verifyWebhook_oldTimestamp_shouldReturnTrue() throws Exception {
        // given - 1년 이내의 과거 타임스탬프는 허용
        String oldTimestamp = String.valueOf(Instant.now().getEpochSecond() - 86400); // 1일 전
        String signedPayload = webhookId + "." + oldTimestamp + "." + body;
        String signature = calculateSignature(signedPayload, webhookSecret);

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, oldTimestamp, "v1," + signature, webhookSecret);

        // then
        assertTrue(result);
    }

    @Test
    void verifyWebhook_emptySecret_shouldReturnTrue() {
        // given
        String emptySecret = "";

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, webhookTimestamp, "v1,any_signature", emptySecret);

        // then
        assertTrue(result); // 시크릿이 없으면 검증을 건너뜀
    }

    @Test
    void verifyWebhook_nullSecret_shouldReturnTrue() {
        // given
        String nullSecret = null;

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, webhookTimestamp, "v1,any_signature", nullSecret);

        // then
        assertTrue(result); // 시크릿이 없으면 검증을 건너뜀
    }

    @Test
    void verifyWebhook_invalidTimestamp_shouldReturnFalse() {
        // given
        String invalidTimestamp = "not-a-number";

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        body, webhookId, invalidTimestamp, "v1,any_signature", webhookSecret);

        // then
        assertFalse(result);
    }

    @Test
    void verifyWebhook_differentBody_shouldReturnFalse() throws Exception {
        // given
        String originalBody = body;
        String differentBody = "{\"type\":\"Transaction.Cancel\",\"data\":{}}";
        String signedPayload = webhookId + "." + webhookTimestamp + "." + originalBody;
        String signature = calculateSignature(signedPayload, webhookSecret);

        // when
        boolean result =
                webhookVerificationService.verifyWebhook(
                        differentBody,
                        webhookId,
                        webhookTimestamp,
                        "v1," + signature,
                        webhookSecret);

        // then
        assertFalse(result);
    }

    private String calculateSignature(String payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
