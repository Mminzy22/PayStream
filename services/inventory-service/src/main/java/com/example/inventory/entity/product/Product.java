package com.example.inventory.entity.product;

import com.example.core.BaseEntity;
import com.example.inventory.entity.inventory.DailyInventory;
import com.example.inventory.entity.store.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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
