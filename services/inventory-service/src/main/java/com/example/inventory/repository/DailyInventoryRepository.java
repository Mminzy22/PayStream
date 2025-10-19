package com.example.inventory.repository;

import com.example.inventory.entity.inventory.DailyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyInventoryRepository extends JpaRepository<DailyInventory, Long> {

    List<DailyInventory> findByProductIdInAndDateBetween(List<Long> productIds, LocalDate checkIn, LocalDate checkOut);

}
