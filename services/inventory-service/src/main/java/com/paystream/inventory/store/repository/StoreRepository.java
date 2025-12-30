package com.paystream.inventory.store.repository;

import com.paystream.inventory.store.entity.Store;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Boolean existsByName(String name);

    List<Store> findByIdIn(List<Long> ids);

    @Query(value = "SELECT distinct s FROM Store s")
    Page<Store> findAllWithPaging(Pageable pageable);
}
