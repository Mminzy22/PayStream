package com.example.inventory.store.dto.request;

import com.example.inventory.store.entity.Address;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreCreateRequest {

//    @NotBlank(message = "hostId는 반드시 입력되어야 합니다.")
    private String hostId;

//    @NotBlank(message = "가게 이름은 반드시 입력해야 합니다.")
    private String name;

//    @NotBlank(message = "도/시 정보는 반드시 입력되어야 합니다.")
    private Address address;

//    @NotBlank(message = "카테고리는 필수 선택 항목입니다.")
    private Category category;

//    @NotNull(message = "체크인 시간은 필수 입력 항목입니다.")
    private LocalTime checkInTime;

//    @NotNull(message = "체크아웃 시간은 필수 입력 항목입니다.")
    private LocalTime checkOutTime;

    private List<Amenities> amenities;
    private int basePersonCount;
    private String rule;

    public Store toEntity() {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .address(address)
                .category(category)
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .amenities(amenities)
                .rules(rule)
                .build();
    }

}
