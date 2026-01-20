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
                    + "WHERE d.product.id in :productIds "
                    + "AND d.date BETWEEN :checkInDate AND :checkOutDate")
    List<DailyInventory> findInvoicesByDateRange(
            List<Long> productIds, LocalDate checkInDate, LocalDate checkOutDate);
}
