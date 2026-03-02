package com.paystream.inventory.promotion.repository;

import com.paystream.inventory.promotion.entity.Promotion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query(
            "select p from Promotion p "
                    + "where ((p.targetType = com.paystream.inventory.promotion.entity.TargetType.STORE and p.targetId in :storeIds) "
                    + "or (p.targetType = com.paystream.inventory.promotion.entity.TargetType.PRODUCT and p.targetId in :productIds)) "
                    + "and p.startDate <= CURRENT_DATE "
                    + "and p.endDate >= CURRENT_DATE")
    List<Promotion> findAllValidPromotions(
            @Param("storeIds") List<Long> storeIds, @Param("productIds") List<Long> productIds);
}
