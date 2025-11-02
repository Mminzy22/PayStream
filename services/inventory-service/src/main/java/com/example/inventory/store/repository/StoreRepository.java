package com.example.inventory.store.repository;

import com.example.inventory.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(value = "select distinct s from Store s",
            countQuery = "select count(distinct s) from Store s")
    Page<Store> findAllByFetchJoin(Pageable pageable);

}
