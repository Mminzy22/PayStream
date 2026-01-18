package com.paystream.inventory.inventory.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paystream.inventory.product.entity.Product;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inventory_seq_gen")
    @SequenceGenerator(
            name = "inventory_seq_gen",
            sequenceName = "inventory_seq", // DB에 생성될 시퀀스 이름
            initialValue = 1,
            allocationSize = 50)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date; // 날짜별 일자

    private int stockAvailable; // 가용 재고

    public void increaseStockAvailable() {
        if (this.stockAvailable >= product.getBaseStock()) {
            throw new IllegalStateException("상품의 기본 재고보다 많습니다.");
        }

        this.stockAvailable++;
    }

    public void decreaseStockAvailable() {
        if (this.stockAvailable <= 0) {
            throw new IllegalStateException("재고가 부족합니다.");
        }

        this.stockAvailable--;
    }

    public boolean isStockAvailable() {
        return stockAvailable != 0;
    }
}
