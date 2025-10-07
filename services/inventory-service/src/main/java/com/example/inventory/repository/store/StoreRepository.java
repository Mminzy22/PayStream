package com.example.inventory.repository.store;

import com.example.inventory.entity.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("SELECT s " +
            "FROM Store s " +
            "LEFT JOIN FETCH s.products "+
            "LEFT JOIN FETCH s.amenities")
    Optional<List<Store>> findAllByFetchJoin();

    @Query("SELECT s "+
            "FROM Store s " +
            "LEFT JOIN FETCH s.products " +
            "LEFT JOIN FETCH s.amenities " +
            "WHERE s.id = :id")
    Optional<Store> findByIdByFetchJoin(@Param("id") UUID id);

}
