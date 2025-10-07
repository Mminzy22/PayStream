package com.example.inventory.entity.inventory;

import com.example.inventory.entity.product.Product;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_hold_history", uniqueConstraints = {
        // '1상품 1이력' 규칙 구현: product_id와 check_in 날짜의 조합은 유일해야 함
        @UniqueConstraint(columnNames = {"product_id", "check_in"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@DynamicUpdate // update시 변경된 필드만 쿼리에 포함
@DynamicInsert // insert시 null은 제외
@SQLRestriction("deleted = false") // 엔티티 검색 시 자동으로 where 절에 추가
@SQLDelete(sql = "update stock_hold_history set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?") // 엔티티 삭제 시 사용할 쿼리 (soft delete)
public class StockHoldHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String bookingId; // 예약ID

    @Column(nullable = false)
    private String userId; // 유저ID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDateTime checkIn;

    @Column(nullable = false)
    private LocalDateTime checkOut;

    @Column(nullable = false)
    private Integer quantity; // 수량

    // Redis에 홀드가 처음 생성된 시간을 애플리케이션에서 직접 설정
    @Column(nullable = false, updatable = false)
    private LocalDateTime createHoldAt;
}
