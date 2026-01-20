package com.paystream.inventory.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDeleteRequest {

    @NotNull(message = "가게는 필수 입력입니다.")
    private Long storeId;
}
