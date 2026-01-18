package com.paystream.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentConfirmDto {

    @NotBlank(message = "포트원 결제 ID는 필수입니다")
    private String impUid;

    @NotBlank(message = "주문 번호는 필수입니다")
    private String merchantUid;

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
}
