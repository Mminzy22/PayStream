package com.paystream.inventory.inventory.repository;

import com.paystream.inventory.inventory.entity.DailyInventory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyInventoryRepository extends JpaRepository<DailyInventory, Long> {

    boolean existsByProductId(Long productId);

    List<DailyInventory> findByProductId(Long productId);

    List<DailyInventory> findByProductIdIn(List<Long> productIds);

    List<DailyInventory> findByProductIdInAndDateGreaterThanEqual(
            List<Long> productIds, LocalDate date);

    @Query(
            "SELECT d FROM DailyInventory d "
                    + "WHERE d.product.id = :productId "
                    + "AND d.date >= :checkInDate "
                    + "AND d.date < :checkOutDate")
    List<DailyInventory> findInventoriesByDateRange(
            Long productId, LocalDate checkInDate, LocalDate checkOutDate);

    @Query(
            value =
                    "SELECT * "
                            + "FROM ( "
                            + "   SELECT *, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY date DESC) as rn "
                            + "   FROM daily_inventory "
                            + ") t "
                            + "WHERE t.rn = 1",
            nativeQuery = true)
    List<DailyInventory> findLatestInventoriesNative();
}
