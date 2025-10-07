package com.example.inventory.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum Amenities {

    WIFI("와이파이"),
    PARKING("주차 가능"),
    SWIMMING_POOL("수영장"),
    RESTAURANT("레스토랑"),
    FITNESS_CENTER("피트니스 센터"),
    BAR_LOUNGE("바/라운지"),
    TWENTY_FOUR_HOUR_DESK("24시간 프론트 데스크"),
    BREAKFAST_INCLUDED("조식 포함"),
    PET_FRIENDLY("반려동물 동반 가능"),
    EV_CHARGING_STATION("전기차 충전소")
    ;

    private final String displayName;

    @JsonValue // Jackson이 이 메서드의 반환값을 JSON 값으로 사용하도록 지정
    public String getDisplayName() {
        return displayName;
    }

}
