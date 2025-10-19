package com.example.inventory.entity.store;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String province;
    private String city;

    public Address(String province, String city) {
        this.province = province;
        this.city = city;
    }
}
