package com.example.inventory.entity.product;

import com.example.core.BaseEntity;
import com.example.inventory.entity.inventory.DailyInventory;
import com.example.inventory.entity.store.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 이 상품이 속한 가게

    // '상품' 하나는 '여러' 날짜별 재고를 가진다.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DailyInventory> dailyInventories = new ArrayList<>();

    private String name;
    private String description;
    private String thumbnail;
    private Integer basePersonCount;
    private Integer maxPersonCount;

    private Long basePrice;
    private Long personAddPrice; // 인원 추가 비용
    private Integer stockTotal; // 총 재고 수

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

}
