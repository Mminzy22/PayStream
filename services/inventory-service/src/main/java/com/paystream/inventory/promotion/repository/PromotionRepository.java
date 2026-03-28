package com.paystream.inventory.promotion.repository;

import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.PromotionStatus;
import com.paystream.inventory.promotion.entity.TargetType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // 프로모션 할인율 계산을 위한 기간내의 특정 Store, Promotion 전체 조회
    @Query(
            "select p from Promotion p "
                    + "where ((p.targetType = com.paystream.inventory.promotion.entity.TargetType.STORE and p.targetId in :storeIds) "
                    + "or (p.targetType = com.paystream.inventory.promotion.entity.TargetType.PRODUCT and p.targetId in :productIds)) "
                    + "and p.startDate <= CURRENT_DATE "
                    + "and p.endDate >= CURRENT_DATE")
    List<Promotion> findAllValidPromotions(
            @Param("storeIds") List<Long> storeIds, @Param("productIds") List<Long> productIds);

    // 동일한 타겟의 타입의 ID가 있는지 검증
    @Query(
            "select count(p) > 0 from Promotion p "
                    + "where p.targetType = :targetType "
                    + "and p.targetId = :targetId "
                    + "and p.startDate <= :endDate "
                    + "and p.endDate >= :startDate")
    boolean existsOverlappingPromotion(
            TargetType targetType, Long targetId, LocalDate startDate, LocalDate endDate);

    @Query(
            "SELECT p FROM Promotion p "
                    + "WHERE (:title IS NULL OR p.title LIKE %:title%) "
                    + "AND (:startDate IS NULL OR p.startDate >= :startDate) "
                    + "AND (:endDate IS NULL OR p.endDate <= :endDate) "
                    + "AND (:status IS NULL OR p.status = :status) "
                    + "ORDER BY p.createdAt DESC")
    Page<Promotion> findAllPromotionWithPaging(
            String title,
            PromotionStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}
