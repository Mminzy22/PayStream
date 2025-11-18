package com.paystream.inventory.store.entity;

import com.paystream.core.BaseEntity;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreUpdateRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@ToString
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate // update시 변경된 필드만 쿼리에 포함
@DynamicInsert // insert시 null은 제외
@SQLRestriction("deleted = false") // 엔티티 검색 시 자동으로 where 절에 추가
@SQLDelete(
        sql =
                "update store set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?") // 엔티티 삭제 시 사용할 쿼리 (soft delete)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostId;
    private String name;
    private String description;

    @Embedded private Address address;

    @Enumerated(EnumType.STRING)
    private Category category;

    // 체크인 시간
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH")
    private LocalTime checkInTime;

    // 체크아웃 시간
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH")
    private LocalTime checkOutTime;

    private int basePersonCount;

    @Builder.Default private double rating = 0.0;

    @Builder.Default private int reviewCount = 0;
    private String rule;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "store_amenities", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "amenity")
    @Enumerated(EnumType.STRING)
    private List<Amenities> amenities = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        this.products.add(product);
        product.setStore(this);
    }

    public void update(StoreUpdateRequest request) {
        this.checkInTime = request.getCheckInTime();
        this.checkOutTime = request.getCheckOutTime();
        this.basePersonCount = request.getBasePersonCount();
        this.amenities = request.getAmenities();
        this.rule = request.getRule();
    }
}
