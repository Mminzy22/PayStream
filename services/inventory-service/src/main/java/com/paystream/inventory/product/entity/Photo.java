package com.paystream.inventory.product.entity;

import com.paystream.core.BaseEntity;
import jakarta.persistence.*;
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
                "update Photo set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?") // 엔티티 삭제 시 사용할 쿼리 (soft delete)
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn
    private Product product;

    @Column(nullable = false)
    private String imagePath;
}
