package com.paystream.payment.service;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final PortOneService portOneService;
    private final UserServiceClient userServiceClient;
    private final String portOneStoreId;
    private final String portOneChannelKey;
    private final String webhookSecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            PortOneService portOneService,
            UserServiceClient userServiceClient,
            @Value("${portone.store-id}") String portOneStoreId,
            @Value("${portone.channel-key}") String portOneChannelKey,
            @Value("${portone.webhook-secret:}") String webhookSecret) {
        this.paymentRepository = paymentRepository;
        this.portOneService = portOneService;
        this.userServiceClient = userServiceClient;
        this.portOneStoreId = portOneStoreId;
        this.portOneChannelKey = portOneChannelKey;
        this.webhookSecret = webhookSecret;
    }

    /** 결제 요청 생성 (결제 대기 상태) */
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto requestDto, Long userId) {
        paymentRepository
                .findByMerchantUid(requestDto.getMerchantUid())
                .ifPresent(
                        payment -> {
                            throw new PayStreamException(ExceptionEnum.PAYMENT_ALREADY_EXISTS);
                        });

        BaseResponse<UserServiceClient.UserInfoResponse> userResponse =
                userServiceClient.getUserById(userId);
        if (userResponse == null || userResponse.getData() == null) {
            throw new PayStreamException(ExceptionEnum.USER_NOT_FOUND);
        }
        UserServiceClient.UserInfoResponse userInfo = userResponse.getData();

        Payment payment = new Payment();
        payment.setMerchantUid(requestDto.getMerchantUid());
        payment.setOrderId(requestDto.getOrderId());
        payment.setUserId(userId);
        payment.setAmount(requestDto.getAmount());
        payment.setName(requestDto.getName());
        payment.setBuyerName(userInfo.name());
        payment.setBuyerEmail(userInfo.email());
        payment.setBuyerTel(userInfo.phone());

        payment.setStatus(Payment.PaymentStatus.READY);

        Payment saved = paymentRepository.save(payment);
        return PaymentResponseDto.from(saved);
    }

    /** 결제 승인 처리 */
    @Transactional
    public PaymentResponseDto confirmPayment(PaymentConfirmDto confirmDto) {

        Payment payment =
                paymentRepository
                        .findByMerchantUid(confirmDto.getMerchantUid())
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PAYMENT_NOT_FOUND));

        PortOnePaymentResponse.PaymentData paymentData =
                portOneService.getPayment(confirmDto.getImpUid());
        if (paymentData == null) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_PORTONE_ERROR);
        }

        if (!paymentData.getMerchantUid().equals(confirmDto.getMerchantUid())) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_MERCHANT_UID_MISMATCH);
        }

        if (payment.getAmount() != null
                && payment.getAmount().compareTo(paymentData.getAmount()) != 0) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_AMOUNT_MISMATCH);
        }

        payment.setPaymentId(paymentData.getImpUid());
        payment.setAmount(paymentData.getAmount());
        payment.setName(paymentData.getName());
        payment.setBuyerName(paymentData.getBuyerName());
        payment.setBuyerEmail(paymentData.getBuyerEmail());
        payment.setBuyerTel(paymentData.getBuyerTel());

        payment.setMethod(Payment.PaymentMethod.CARD);

        updatePaymentStatus(payment, paymentData);

        Payment saved = paymentRepository.save(payment);
        return PaymentResponseDto.from(saved);
    }

    /** 결제 조회 */
    public PaymentResponseDto getPayment(Long id) {
        Payment payment =
                paymentRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PAYMENT_NOT_FOUND));

        if (payment.getPaymentId() != null) {
            syncPaymentStatusFromPortOne(payment);
        }

        return PaymentResponseDto.from(payment);
    }

    /** 결제 조회 (포트원 ID로) */
    public PaymentResponseDto getPaymentByImpUid(String impUid) {
        Payment payment =
                paymentRepository
                        .findByPaymentId(impUid)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PAYMENT_NOT_FOUND));

        syncPaymentStatusFromPortOne(payment);

        return PaymentResponseDto.from(payment);
    }

    /** 포트원 API에서 결제 상태를 동기화합니다. */
    private void syncPaymentStatusFromPortOne(Payment payment) {
        try {
            PortOnePaymentResponse.PaymentData paymentData =
                    portOneService.getPayment(payment.getMerchantUid());
            if (paymentData == null) {
                return;
            }

            String portOneStatus = paymentData.getStatus();
            boolean needsUpdate = false;

            if ("cancelled".equals(portOneStatus)
                    && payment.getStatus() != Payment.PaymentStatus.CANCELLED) {
                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                payment.setFailureReason(paymentData.getCancelReason());
                needsUpdate = true;
            } else if ("paid".equals(portOneStatus)
                    && payment.getStatus() != Payment.PaymentStatus.PAID) {
                payment.setStatus(Payment.PaymentStatus.PAID);
                needsUpdate = true;
            } else if ("failed".equals(portOneStatus)
                    && payment.getStatus() != Payment.PaymentStatus.FAILED) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(paymentData.getFailReason());
                needsUpdate = true;
            }

            if (needsUpdate) {
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
        }
    }

    /** 결제 상태 업데이트 (공통 로직) */
    private void updatePaymentStatus(
            Payment payment, PortOnePaymentResponse.PaymentData paymentData) {
        if ("paid".equals(paymentData.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.PAID);
            if (paymentData.getPaidAt() != null) {
                payment.setPaidAt(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(paymentData.getPaidAt()),
                                ZoneId.systemDefault()));
            }
        } else if ("cancelled".equals(paymentData.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.CANCELLED);
            payment.setFailureReason(paymentData.getCancelReason());
        } else if ("failed".equals(paymentData.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(paymentData.getFailReason());
        } else {
            payment.setStatus(Payment.PaymentStatus.READY);
        }
    }

    /** 결제 취소 */
    @Transactional
    public PaymentResponseDto cancelPayment(String impUid, String reason) {
        Payment payment =
                paymentRepository
                        .findByPaymentId(impUid)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_NOT_PAID);
        }

        // 포트원 V2 API는 merchant_uid를 paymentId로 사용
        portOneService.cancelPayment(payment.getMerchantUid(), reason);

        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);
        Payment saved = paymentRepository.save(payment);

        return PaymentResponseDto.from(saved);
    }

    /** 주문 ID로 결제 목록 조회 */
    public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(PaymentResponseDto::from)
                .collect(Collectors.toList());
    }

    /** 사용자별 결제 목록 조회 */
    public List<PaymentResponseDto> getAllPaymentsByUserId(
            Long userId, Integer page, Integer size) {
        List<Payment> payments = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        int pageNum = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 20;
        int start = pageNum * pageSize;
        int end = Math.min(start + pageSize, payments.size());

        List<Payment> pagedPayments =
                start < payments.size() ? payments.subList(start, end) : new ArrayList<>();

        for (Payment payment : pagedPayments) {
            if (payment.getPaymentId() != null) {
                syncPaymentStatusFromPortOne(payment);
            }
        }

        return pagedPayments.stream().map(PaymentResponseDto::from).collect(Collectors.toList());
    }

    public String getPortOneStoreId() {
        return portOneStoreId;
    }

    public String getPortOneChannelKey() {
        return portOneChannelKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    /** 웹훅으로부터 결제 정보 동기화 */
    @Transactional
    public void syncPaymentFromWebhook(String paymentId) {
        try {
            PortOnePaymentResponse.PaymentData paymentData = portOneService.getPayment(paymentId);

            if (paymentData == null) {
                log.warn("웹훅: 포트원 결제 정보 조회 실패: paymentId={}", paymentId);
                return;
            }

            Payment payment =
                    paymentRepository
                            .findByMerchantUid(paymentData.getMerchantUid())
                            .orElseGet(
                                    () -> {
                                        Payment newPayment = new Payment();
                                        newPayment.setMerchantUid(paymentData.getMerchantUid());
                                        return newPayment;
                                    });

            payment.setPaymentId(paymentData.getImpUid());
            payment.setAmount(paymentData.getAmount());
            payment.setName(paymentData.getName());
            payment.setBuyerName(paymentData.getBuyerName());
            payment.setBuyerEmail(paymentData.getBuyerEmail());
            payment.setBuyerTel(paymentData.getBuyerTel());
            payment.setMethod(Payment.PaymentMethod.CARD);
            if ("paid".equals(paymentData.getStatus())) {
                payment.setStatus(Payment.PaymentStatus.PAID);
                if (paymentData.getPaidAt() != null) {
                    payment.setPaidAt(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(paymentData.getPaidAt()),
                                    ZoneId.systemDefault()));
                }
            } else if ("cancelled".equals(paymentData.getStatus())) {
                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                payment.setFailureReason(paymentData.getCancelReason());
            } else if ("failed".equals(paymentData.getStatus())) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason(paymentData.getFailReason());
            } else {
                payment.setStatus(Payment.PaymentStatus.READY);
            }

            paymentRepository.save(payment);
            log.info("웹훅: 결제 정보 동기화 완료: paymentId={}, status={}", paymentId, payment.getStatus());

        } catch (Exception e) {
            log.error("웹훅: 결제 정보 동기화 실패: paymentId={}, error={}", paymentId, e.getMessage(), e);
            throw e;
        }
    }
}
