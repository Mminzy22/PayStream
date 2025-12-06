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

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private String name;
    private String description;
    private String thumbnail;
    private int minCapacity; // 최소 수용인원
    private int maxCapacity; // 최대 수용인원

    private int basePrice;
    private int personAddPrice; // 인원 추가 비용

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
}
