package com.paystream.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 포트원 웹훅 검증 서비스 Standard Webhooks 스펙을 따라 웹훅 서명을 검증합니다. */
@Service
public class WebhookVerificationService {

    private static final Logger log = LoggerFactory.getLogger(WebhookVerificationService.class);

    /** 웹훅 서명 검증 */
    public boolean verifyWebhook(
            String body,
            String webhookId,
            String webhookTimestamp,
            String webhookSignature,
            String webhookSecret) {
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("웹훅 시크릿이 설정되지 않았습니다. 웹훅 검증을 건너뜁니다.");
            return true;
        }

        try {
            long timestamp = Long.parseLong(webhookTimestamp);
            long currentTime = Instant.now().getEpochSecond();
            long timeDifference = currentTime - timestamp;

            if (timeDifference < -300) {
                log.warn("웹훅 타임스탬프가 5분 이상 미래입니다: 차이={}초", timeDifference);
                return false;
            }

            if (timeDifference > 31536000) {
                log.warn("웹훅 타임스탬프가 1년 이상 과거입니다: 차이={}초 (일괄 재발송 가능성)", timeDifference);
            }

            String signedPayload = webhookId + "." + webhookTimestamp + "." + body;
            String expectedSignature = calculateSignature(signedPayload, webhookSecret);

            String actualSignature;
            if (webhookSignature.startsWith("v1,")) {
                actualSignature = webhookSignature.substring(3);
            } else {
                actualSignature = webhookSignature;
            }

            boolean isValid = constantTimeEquals(expectedSignature, actualSignature);
            if (!isValid) {
                log.warn("웹훅 서명 검증 실패: webhookId={}", webhookId);
            }

            return isValid;

        } catch (Exception e) {
            log.error("웹훅 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /** HMAC SHA256 서명 계산 */
    private String calculateSignature(String payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec =
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /** 상수 시간 문자열 비교 (타이밍 공격 방지) */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
