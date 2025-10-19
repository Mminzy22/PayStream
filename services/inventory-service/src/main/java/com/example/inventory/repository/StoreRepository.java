package com.example.inventory.repository;

import com.example.inventory.entity.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(value = "select distinct s from Store s",
            countQuery = "select count(distinct s) from Store s")
    Page<Store> findAllByFetchJoin(Pageable pageable);

}
