package com.paystream.inventory.store.dto.request;

import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreListFindRequest {

    private String name;
    private String province;
    private String city;
    private Category category;
    private List<Amenities> amenities;

    @Min(value = 2, message = "인원수는 최소 2명 이상이어야 합니다.")
    private int personCount;

    @NotNull(message = "체크인 날짜는 반드시 입력해야 합니다.")
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜는 반드시 입력해야 합니다.")
    private LocalDate checkOutDate;

    public String getCacheKey(int page) {
        return String.format(
                "%s-%s-%s-%s-%d-%s-%s-%d",
                name, province, city, category, personCount, checkInDate, checkOutDate, page);
    }
}
