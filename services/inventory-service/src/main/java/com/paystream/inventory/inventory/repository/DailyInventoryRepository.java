package com.paystream.inventory.inventory.repository;

import com.paystream.inventory.inventory.entity.DailyInventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyInventoryRepository extends JpaRepository<DailyInventory, Long> {

    boolean existsByProductId(Long productId);

    List<DailyInventory> findByProductId(Long productId);
}
