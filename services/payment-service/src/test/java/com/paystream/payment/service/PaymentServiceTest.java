package com.paystream.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.paystream.core.BaseResponse;
import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.payment.client.UserServiceClient;
import com.paystream.payment.dto.PaymentConfirmDto;
import com.paystream.payment.dto.PaymentRequestDto;
import com.paystream.payment.dto.PaymentResponseDto;
import com.paystream.payment.dto.PortOnePaymentResponse;
import com.paystream.payment.entity.Payment;
import com.paystream.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;

    @Mock private PortOneService portOneService;

    @Mock private UserServiceClient userServiceClient;

    private PaymentService paymentService;

    private static final String PORTONE_STORE_ID = "test-store-id";
    private static final String PORTONE_CHANNEL_KEY = "test-channel-key";
    private static final String WEBHOOK_SECRET = "test-webhook-secret";

    @BeforeEach
    void setUp() {
        paymentService =
                new PaymentService(
                        paymentRepository,
                        portOneService,
                        userServiceClient,
                        PORTONE_STORE_ID,
                        PORTONE_CHANNEL_KEY,
                        WEBHOOK_SECRET);
    }

    @Test
    void createPayment_success_shouldReturnPaymentResponse() {
        // given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setMerchantUid("merchant_123");
        requestDto.setOrderId(1L);
        requestDto.setAmount(new BigDecimal("10000"));
        requestDto.setName("테스트 상품");

        Long userId = 1L;
        UserServiceClient.UserInfoResponse userInfo =
                new UserServiceClient.UserInfoResponse(
                        userId, "test@example.com", "홍길동", "010-1234-5678");

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.empty());
        when(userServiceClient.getUserById(userId)).thenReturn(BaseResponse.ok(userInfo));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(
                        invocation -> {
                            Payment payment = invocation.getArgument(0);
                            payment.setId(1L);
                            return payment;
                        });

        // when
        PaymentResponseDto result = paymentService.createPayment(requestDto, userId);

        // then
        assertNotNull(result);
        assertEquals("merchant_123", result.getMerchantUid());
        assertEquals(new BigDecimal("10000"), result.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_duplicateMerchantUid_shouldThrowException() {
        // given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setMerchantUid("merchant_123");

        Payment existingPayment = new Payment();
        existingPayment.setMerchantUid("merchant_123");

        when(paymentRepository.findByMerchantUid("merchant_123"))
                .thenReturn(Optional.of(existingPayment));

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class,
                        () -> paymentService.createPayment(requestDto, 1L));

        assertEquals(ExceptionEnum.PAYMENT_ALREADY_EXISTS, exception.getError());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_userNotFound_shouldThrowException() {
        // given
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setMerchantUid("merchant_123");

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.empty());
        when(userServiceClient.getUserById(1L)).thenReturn(null);

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class,
                        () -> paymentService.createPayment(requestDto, 1L));

        assertEquals(ExceptionEnum.USER_NOT_FOUND, exception.getError());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void confirmPayment_success_shouldReturnPaymentResponse() {
        // given
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setMerchantUid("merchant_123");
        confirmDto.setImpUid("imp_123");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setMerchantUid("merchant_123");
        payment.setAmount(new BigDecimal("10000"));
        payment.setStatus(Payment.PaymentStatus.READY);

        PortOnePaymentResponse.PaymentData paymentData = new PortOnePaymentResponse.PaymentData();
        paymentData.setImpUid("imp_123");
        paymentData.setMerchantUid("merchant_123");
        paymentData.setAmount(new BigDecimal("10000"));
        paymentData.setStatus("paid");
        paymentData.setName("테스트 상품");
        paymentData.setBuyerName("홍길동");
        paymentData.setBuyerEmail("test@example.com");
        paymentData.setBuyerTel("010-1234-5678");
        paymentData.setPaidAt(Instant.now().getEpochSecond());

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.of(payment));
        when(portOneService.getPayment("imp_123")).thenReturn(paymentData);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        PaymentResponseDto result = paymentService.confirmPayment(confirmDto);

        // then
        assertNotNull(result);
        assertEquals("imp_123", result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void confirmPayment_paymentNotFound_shouldThrowException() {
        // given
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setMerchantUid("merchant_123");

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.empty());

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class, () -> paymentService.confirmPayment(confirmDto));

        assertEquals(ExceptionEnum.PAYMENT_NOT_FOUND, exception.getError());
    }

    @Test
    void confirmPayment_merchantUidMismatch_shouldThrowException() {
        // given
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setMerchantUid("merchant_123");
        confirmDto.setImpUid("imp_123");

        Payment payment = new Payment();
        payment.setMerchantUid("merchant_123");

        PortOnePaymentResponse.PaymentData paymentData = new PortOnePaymentResponse.PaymentData();
        paymentData.setMerchantUid("merchant_456"); // 다른 merchantUid

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.of(payment));
        when(portOneService.getPayment("imp_123")).thenReturn(paymentData);

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class, () -> paymentService.confirmPayment(confirmDto));

        assertEquals(ExceptionEnum.PAYMENT_MERCHANT_UID_MISMATCH, exception.getError());
    }

    @Test
    void confirmPayment_amountMismatch_shouldThrowException() {
        // given
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setMerchantUid("merchant_123");
        confirmDto.setImpUid("imp_123");

        Payment payment = new Payment();
        payment.setMerchantUid("merchant_123");
        payment.setAmount(new BigDecimal("10000"));

        PortOnePaymentResponse.PaymentData paymentData = new PortOnePaymentResponse.PaymentData();
        paymentData.setMerchantUid("merchant_123");
        paymentData.setAmount(new BigDecimal("20000")); // 다른 금액

        when(paymentRepository.findByMerchantUid("merchant_123")).thenReturn(Optional.of(payment));
        when(portOneService.getPayment("imp_123")).thenReturn(paymentData);

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class, () -> paymentService.confirmPayment(confirmDto));

        assertEquals(ExceptionEnum.PAYMENT_AMOUNT_MISMATCH, exception.getError());
    }

    @Test
    void getPayment_success_shouldReturnPaymentResponse() {
        // given
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setMerchantUid("merchant_123");
        payment.setAmount(new BigDecimal("10000"));
        payment.setStatus(Payment.PaymentStatus.PAID);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // when
        PaymentResponseDto result = paymentService.getPayment(paymentId);

        // then
        assertNotNull(result);
        assertEquals(paymentId, result.getId());
    }

    @Test
    void getPayment_notFound_shouldThrowException() {
        // given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // when & then
        PayStreamException exception =
                assertThrows(PayStreamException.class, () -> paymentService.getPayment(paymentId));

        assertEquals(ExceptionEnum.PAYMENT_NOT_FOUND, exception.getError());
    }

    @Test
    void cancelPayment_success_shouldReturnPaymentResponse() {
        // given
        String impUid = "imp_123";
        String merchantUid = "merchant_123";
        String reason = "고객 요청";

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentId(impUid);
        payment.setMerchantUid(merchantUid);
        payment.setStatus(Payment.PaymentStatus.PAID);

        when(paymentRepository.findByPaymentId(impUid)).thenReturn(Optional.of(payment));
        doNothing().when(portOneService).cancelPayment(merchantUid, reason);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        PaymentResponseDto result = paymentService.cancelPayment(impUid, reason);

        // then
        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.CANCELLED, payment.getStatus());
        verify(portOneService, times(1)).cancelPayment(merchantUid, reason);
    }

    @Test
    void cancelPayment_notPaid_shouldThrowException() {
        // given
        String impUid = "imp_123";
        Payment payment = new Payment();
        payment.setPaymentId(impUid);
        payment.setStatus(Payment.PaymentStatus.READY); // 아직 결제되지 않음

        when(paymentRepository.findByPaymentId(impUid)).thenReturn(Optional.of(payment));

        // when & then
        PayStreamException exception =
                assertThrows(
                        PayStreamException.class,
                        () -> paymentService.cancelPayment(impUid, "reason"));

        assertEquals(ExceptionEnum.PAYMENT_NOT_PAID, exception.getError());
        verify(portOneService, never()).cancelPayment(anyString(), anyString());
    }
}
