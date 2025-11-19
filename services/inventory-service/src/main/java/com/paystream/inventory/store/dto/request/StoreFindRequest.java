package com.paystream.inventory.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreFindRequest {

    @Min(value = 2, message = "인원수는 최소 2명 이상이어야 합니다.")
    private int personCount;

    @NotNull(message = "체크인 날짜는 반드시 입력해야 합니다.")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 반드시 입력해야 합니다.")
    private LocalDate checkOutDate;
}
