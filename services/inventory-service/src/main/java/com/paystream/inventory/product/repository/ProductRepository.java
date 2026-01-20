package com.paystream.inventory.product.repository;

import com.paystream.inventory.product.entity.Product;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByStoreIdAndName(Long storeId, String name);

    @Query(
            value =
                    "SELECT DISTINCT p FROM Product p "
                            + "JOIN p.dailyInventories i "
                            + "WHERE p.store.id IN :storeIds "
                            + "AND i.date >= :checkInDate AND i.date < :checkOutDate")
    List<Product> findAvailableProducts(
            List<Long> storeIds, LocalDate checkInDate, LocalDate checkOutDate);
}
