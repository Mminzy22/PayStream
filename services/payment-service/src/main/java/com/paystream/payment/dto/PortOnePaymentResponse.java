package com.paystream.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public class PortOnePaymentResponse {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("response")
    private PaymentData response;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PaymentData getResponse() {
        return response;
    }

    public void setResponse(PaymentData response) {
        this.response = response;
    }

    public static class PaymentData {
        @JsonProperty("imp_uid")
        private String impUid;

        @JsonProperty("merchant_uid")
        private String merchantUid;

        @JsonProperty("pay_method")
        private String payMethod;

        @JsonProperty("channel")
        private String channel;

        @JsonProperty("pg_provider")
        private String pgProvider;

        @JsonProperty("emb_pg_provider")
        private String embPgProvider;

        @JsonProperty("pg_tid")
        private String pgTid;

        @JsonProperty("pg_id")
        private String pgId;

        @JsonProperty("escrow")
        private Boolean escrow;

        @JsonProperty("apply_num")
        private String applyNum;

        @JsonProperty("bank_code")
        private String bankCode;

        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("card_code")
        private String cardCode;

        @JsonProperty("card_name")
        private String cardName;

        @JsonProperty("card_quota")
        private Integer cardQuota;

        @JsonProperty("card_number")
        private String cardNumber;

        @JsonProperty("card_type")
        private Integer cardType;

        @JsonProperty("vbank_code")
        private String vbankCode;

        @JsonProperty("vbank_name")
        private String vbankName;

        @JsonProperty("vbank_num")
        private String vbankNum;

        @JsonProperty("vbank_holder")
        private String vbankHolder;

        @JsonProperty("vbank_date")
        private Long vbankDate;

        @JsonProperty("vbank_issued_at")
        private Long vbankIssuedAt;

        @JsonProperty("name")
        private String name;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("cancel_amount")
        private BigDecimal cancelAmount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("buyer_name")
        private String buyerName;

        @JsonProperty("buyer_tel")
        private String buyerTel;

        @JsonProperty("buyer_email")
        private String buyerEmail;

        @JsonProperty("buyer_addr")
        private String buyerAddr;

        @JsonProperty("buyer_postcode")
        private String buyerPostcode;

        @JsonProperty("custom_data")
        private String customData;

        @JsonProperty("user_agent")
        private String userAgent;

        @JsonProperty("status")
        private String status;

        @JsonProperty("started_at")
        private Long startedAt;

        @JsonProperty("paid_at")
        private Long paidAt;

        @JsonProperty("failed_at")
        private Long failedAt;

        @JsonProperty("cancelled_at")
        private Long cancelledAt;

        @JsonProperty("fail_reason")
        private String failReason;

        @JsonProperty("cancel_reason")
        private String cancelReason;

        @JsonProperty("receipt_url")
        private String receiptUrl;

        @JsonProperty("cancel_history")
        private Object[] cancelHistory;

        @JsonProperty("cancel_receipt_urls")
        private String[] cancelReceiptUrls;

        @JsonProperty("cash_receipt_issued")
        private Boolean cashReceiptIssued;

        @JsonProperty("customer_uid")
        private String customerUid;

        @JsonProperty("customer_uid_usage")
        private String customerUidUsage;

        @JsonProperty("custom_data")
        private Map<String, Object> customDataMap;

        // Getters and Setters
        public String getImpUid() {
            return impUid;
        }

        public void setImpUid(String impUid) {
            this.impUid = impUid;
        }

        public String getMerchantUid() {
            return merchantUid;
        }

        public void setMerchantUid(String merchantUid) {
            this.merchantUid = merchantUid;
        }

        public String getPayMethod() {
            return payMethod;
        }

        public void setPayMethod(String payMethod) {
            this.payMethod = payMethod;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
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

        public Long getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(Long paidAt) {
            this.paidAt = paidAt;
        }

        public String getFailReason() {
            return failReason;
        }

        public void setFailReason(String failReason) {
            this.failReason = failReason;
        }

        public String getCancelReason() {
            return cancelReason;
        }

        public void setCancelReason(String cancelReason) {
            this.cancelReason = cancelReason;
        }
    }
}
