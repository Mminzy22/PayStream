package com.paystream.inventory.store.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    HOTEL("호텔"),
    PENSION("펜션"),
    POOL_VILLA("풀빌라"),
    GLAMPING("글램핑");

    private final String displayName;
}
