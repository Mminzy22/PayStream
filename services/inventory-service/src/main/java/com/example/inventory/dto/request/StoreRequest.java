package com.example.inventory.dto.request;

import com.example.inventory.entity.Amenities;
import com.example.inventory.entity.Category;
import com.example.inventory.entity.store.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRequest {

    @NotBlank(message = "호스트 ID는 필수입니다.")
    private String hostId;

    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;

    private String description;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotNull(message = "체크인 시간은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @NotNull(message = "체크아웃 시간은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    // 편의시설은 중복이 없으므로 Set으로 관리
    @NotNull(message = "편의시설은 1개 이상 등록해야 합니다.")
    private Set<String> amenities;

    @NotEmpty(message = "사진은 최소 1장 이상 등록해야 합니다.")
    private List<String> photos;

    private String rules; // 이용 규칙

    @Builder
    private StoreRequest(String hostId, String name, String description, String address, String category, LocalDateTime checkInTime, LocalDateTime checkOutTime, Set<String> amenities, List<String> photos, String rules) {
        this.hostId = hostId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.category = category;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.amenities = amenities;
        this.photos = photos;
        this.rules = rules;
    }

    public Store toEntity() {
        return Store.builder()
                .hostId(this.hostId)
                .name(this.name)
                .description(this.description)
                .address(this.address)
                .category(Category.valueOf(this.category))
                .checkInTime(this.checkInTime)
                .checkOutTime(this.checkOutTime)
                // Set<String>을 Set<Amenities> Enum으로 변환
                .amenities(
                        this.amenities.stream()
                                .map(Amenities::valueOf) // 문자열을 Enum 상수로 변환
                                .collect(Collectors.toSet())
                )
                .rules(this.rules)
                .build();
    }

}
