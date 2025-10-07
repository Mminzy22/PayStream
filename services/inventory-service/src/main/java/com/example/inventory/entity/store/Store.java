package com.example.inventory.entity.store;

import com.example.core.BaseEntity;
import com.example.inventory.dto.StoreDto;
import com.example.inventory.entity.Amenities;
import com.example.inventory.entity.Category;
import com.example.inventory.entity.product.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@DynamicUpdate // update시 변경된 필드만 쿼리에 포함
@DynamicInsert // insert시 null은 제외
@SQLRestriction("deleted = false") // 엔티티 검색 시 자동으로 where 절에 추가
@SQLDelete(sql = "update store set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?") // 엔티티 삭제 시 사용할 쿼리 (soft delete)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String hostId;

    @Column(nullable = false)
    private String name;
    private String description;
    private String address;

    @Enumerated(EnumType.STRING)
    private Category category; // 호텔, 펜션, 리조트

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "store_amenities",  // 1. 생성될 테이블 이름을 'store_amenities'로 지정
            joinColumns = @JoinColumn(name = "store_id") // 2. 부모를 가리킬 FK 컬럼명을 'store_id'로 지정
    )
    @Column(name = "amenity") // 3. Enum 값이 저장될 컬럼명을 'amenity'로 지정
    @Enumerated(EnumType.STRING)
    private Set<Amenities> amenities = new HashSet<>(); // 부대시설

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    private Double rating; // 평균 별점
    private Integer reviewCount; // 리뷰 수

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    private String rules; // 이용규칙

    public void update(StoreDto.Request request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.address = request.getAddress();
        this.category = Category.valueOf(request.getCategory());
        this.amenities = request.getAmenities().stream().map(Amenities::valueOf).collect(Collectors.toSet());
        this.checkInTime = request.getCheckInTime();
        this.checkOutTime = request.getCheckOutTime();
        this.rules = request.getRules();
    }

}
