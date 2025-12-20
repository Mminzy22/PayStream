package com.paystream.inventory.product.entity;

import com.paystream.core.BaseEntity;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.store.entity.Store;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate // update시 변경된 필드만 쿼리에 포함
@DynamicInsert // insert시 null은 제외
@SQLRestriction("deleted = false") // 엔티티 검색 시 자동으로 where 절에 추가
@SQLDelete(
        sql =
                "update product set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?") // 엔티티 삭제 시 사용할 쿼리 (soft delete)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    //    private String thumbnail;

    @Column(nullable = false)
    private int minCapacity; // 최소 수용인원

    @Column(nullable = false)
    private int maxCapacity; // 최대 수용인원

    @Column(nullable = false)
    private int basePrice;

    @Column(nullable = false)
    private int personAddPrice; // 인원 추가 비용

    @Column(nullable = false)
    private int baseStock;

    // '상품' 하나는 '여러' 날짜별 재고를 가진다.
    @Builder.Default
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<DailyInventory> dailyInventories = new ArrayList<>();

    //    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Photo> photos = new ArrayList<>();

    public void addDailyInventory(DailyInventory dailyInventory) {
        dailyInventories.add(dailyInventory);
        dailyInventory.setProduct(this);
    }

    public void assignStore(Store store) {
        // 기존 가게와의 관계를 끊는 로직
        if (this.store != null) {
            this.store.getProducts().remove(this);
        }

        this.store = store;

        if (store != null) {
            store.getProducts().add(this);
        }
    }

    public void updateInfo(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.minCapacity = product.getMinCapacity();
        this.maxCapacity = product.getMaxCapacity();
        this.basePrice = product.getBasePrice();
        this.personAddPrice = product.getPersonAddPrice();
    }
}
