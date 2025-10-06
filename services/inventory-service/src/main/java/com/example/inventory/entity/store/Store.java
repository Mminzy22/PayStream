package com.example.inventory.entity.store;

import com.example.inventory.entity.product.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category; // 호텔, 펜션, 리조트

    @Enumerated(EnumType.STRING)
    private List<Amenities> amenities; // 부대시설

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    private String status;
    private Double rating;
    private Integer reviewCount;

    @CreatedBy
    @Column(updatable = false) // 생성된 이후에는 수정되지 않도록 설정
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedBy
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

}
