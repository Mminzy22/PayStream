package com.paystream.payment.dto;

import com.paystream.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public class PaymentResponseDto {

    private Long id;
    private String paymentId;
    private String merchantUid;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String method;
    private String buyerName;
    private String buyerEmail;
    private String buyerTel;
    private String name;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentResponseDto from(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setPaymentId(payment.getPaymentId());
        dto.setMerchantUid(payment.getMerchantUid());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().name());
        dto.setMethod(payment.getMethod() != null ? payment.getMethod().name() : null);
        dto.setBuyerName(payment.getBuyerName());
        dto.setBuyerEmail(payment.getBuyerEmail());
        dto.setBuyerTel(payment.getBuyerTel());
        dto.setName(payment.getName());
        dto.setFailureReason(payment.getFailureReason());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    /** 포트원 API 응답에서 PaymentResponseDto로 변환 */
    public static PaymentResponseDto fromPortOne(
            PortOnePaymentResponse.PaymentData portOnePayment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(null);
        dto.setPaymentId(portOnePayment.getImpUid());
        dto.setMerchantUid(portOnePayment.getMerchantUid());
        dto.setOrderId(null);
        dto.setAmount(portOnePayment.getAmount());
        dto.setStatus(
                portOnePayment.getStatus() != null
                        ? portOnePayment.getStatus().toUpperCase()
                        : null);
        dto.setMethod("CARD");
        dto.setBuyerName(portOnePayment.getBuyerName());
        dto.setBuyerEmail(portOnePayment.getBuyerEmail());
        dto.setBuyerTel(portOnePayment.getBuyerTel());
        dto.setName(portOnePayment.getName());
        dto.setFailureReason(
                portOnePayment.getCancelReason() != null
                        ? portOnePayment.getCancelReason()
                        : portOnePayment.getFailReason());

        if (portOnePayment.getPaidAt() != null) {
            dto.setPaidAt(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(portOnePayment.getPaidAt()),
                            java.time.ZoneId.systemDefault()));
        }

        dto.setCreatedAt(null);

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getMerchantUid() {
        return merchantUid;
    }

    public void setMerchantUid(String merchantUid) {
        this.merchantUid = merchantUid;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerTel() {
        return buyerTel;
    }

    public void setBuyerTel(String buyerTel) {
        this.buyerTel = buyerTel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
