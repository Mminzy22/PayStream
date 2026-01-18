package com.paystream.payment.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.paystream.core.BaseResponse;
import com.paystream.payment.filter.CachedBodyHttpServletRequest;
import com.paystream.payment.service.PaymentService;
import com.paystream.payment.service.WebhookVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortOneWebhookControllerTest {

    @Mock private PaymentService paymentService;

    @Mock private WebhookVerificationService webhookVerificationService;

    @Mock private HttpServletRequest httpServletRequest;

    private PortOneWebhookController controller;

    private String webhookBody;
    private String webhookId;
    private String webhookTimestamp;
    private String webhookSignature;

    @BeforeEach
    void setUp() throws IOException {
        controller = new PortOneWebhookController(paymentService, webhookVerificationService);

        webhookBody = "{\"type\":\"Transaction.Payment\",\"data\":{\"paymentId\":\"imp_123456\"}}";
        webhookId = "evt_1234567890";
        webhookTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        webhookSignature = "v1,test_signature";
    }

    @Test
    void handleWebhook_validRequest_shouldReturnOk() throws Exception {
        // given
        when(paymentService.getWebhookSecret()).thenReturn("test-secret");
        when(webhookVerificationService.verifyWebhook(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        CachedBodyHttpServletRequest cachedRequest = createCachedBodyRequest(webhookBody);

        // when
        BaseResponse<Map<String, String>> result =
                controller.handleWebhook(
                        cachedRequest, webhookId, webhookTimestamp, webhookSignature);

        // then
        assertNotNull(result);
        assertEquals("ok", result.getData().get("status"));
        verify(paymentService, times(1)).syncPaymentFromWebhook("imp_123456");
    }

    @Test
    void handleWebhook_invalidSignature_shouldReturnError() throws Exception {
        // given
        when(paymentService.getWebhookSecret()).thenReturn("test-secret");
        when(webhookVerificationService.verifyWebhook(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        CachedBodyHttpServletRequest cachedRequest = createCachedBodyRequest(webhookBody);

        // when
        BaseResponse<Map<String, String>> result =
                controller.handleWebhook(
                        cachedRequest, webhookId, webhookTimestamp, "v1,invalid_signature");

        // then
        assertNotNull(result);
        assertEquals("error", result.getData().get("status"));
        verify(paymentService, never()).syncPaymentFromWebhook(anyString());
    }

    @Test
    void handleWebhook_missingPaymentId_shouldReturnOk() throws IOException {
        // given
        String bodyWithoutPaymentId = "{\"type\":\"Transaction.Payment\",\"data\":{}}";
        when(paymentService.getWebhookSecret()).thenReturn("test-secret");
        when(webhookVerificationService.verifyWebhook(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        CachedBodyHttpServletRequest cachedRequest = createCachedBodyRequest(bodyWithoutPaymentId);

        // when
        BaseResponse<Map<String, String>> result =
                controller.handleWebhook(
                        cachedRequest, webhookId, webhookTimestamp, webhookSignature);

        // then
        assertNotNull(result);
        assertEquals("ok", result.getData().get("status"));
        verify(paymentService, never()).syncPaymentFromWebhook(anyString());
    }

    @Test
    void handleWebhook_nonTransactionEvent_shouldReturnOk() throws IOException {
        // given
        String nonTransactionBody =
                "{\"type\":\"Other.Event\",\"data\":{\"paymentId\":\"imp_123456\"}}";
        when(paymentService.getWebhookSecret()).thenReturn("test-secret");
        when(webhookVerificationService.verifyWebhook(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        CachedBodyHttpServletRequest cachedRequest = createCachedBodyRequest(nonTransactionBody);

        // when
        BaseResponse<Map<String, String>> result =
                controller.handleWebhook(
                        cachedRequest, webhookId, webhookTimestamp, webhookSignature);

        // then
        assertNotNull(result);
        assertEquals("ok", result.getData().get("status"));
        verify(paymentService, never()).syncPaymentFromWebhook(anyString());
    }

    @Test
    void handleWebhook_exception_shouldReturnError() throws Exception {
        // given
        when(paymentService.getWebhookSecret()).thenReturn("test-secret");
        when(webhookVerificationService.verifyWebhook(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Test exception"));

        CachedBodyHttpServletRequest cachedRequest = createCachedBodyRequest(webhookBody);

        // when
        BaseResponse<Map<String, String>> result =
                controller.handleWebhook(
                        cachedRequest, webhookId, webhookTimestamp, webhookSignature);

        // then
        assertNotNull(result);
        assertEquals("error", result.getData().get("status"));
    }

    private CachedBodyHttpServletRequest createCachedBodyRequest(String body) throws IOException {
        when(httpServletRequest.getInputStream())
                .thenReturn(new MockServletInputStream(new ByteArrayInputStream(body.getBytes())));
        return new CachedBodyHttpServletRequest(httpServletRequest);
    }

    private static class MockServletInputStream extends jakarta.servlet.ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}
