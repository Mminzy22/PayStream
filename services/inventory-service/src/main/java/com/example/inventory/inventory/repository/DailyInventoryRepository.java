package com.example.inventory.inventory.repository;

import com.example.inventory.inventory.entity.DailyInventory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyInventoryRepository extends JpaRepository<DailyInventory, Long> {

    List<DailyInventory> findByProductIdInAndDateBetween(
            List<Long> productIds, LocalDate checkIn, LocalDate checkOut);
}
