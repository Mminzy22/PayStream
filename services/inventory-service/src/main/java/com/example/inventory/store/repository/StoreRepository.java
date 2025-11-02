package com.example.inventory.store.repository;

import com.example.inventory.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Boolean existsByName(String name);

}
