package com.example.inventory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DealStatus {

    ACTIVE("진행중"),
    END("종료"),
    PAUSED("일시정지")
    ;

    private final String status;
}
