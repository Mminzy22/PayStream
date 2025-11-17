package com.example.inventory.store.repository;

import com.example.inventory.store.dto.request.StoreListFindRequest;
import com.example.inventory.store.entity.Store;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreQueryDslRepository {

    Page<Store> findAllByFetchJoin(StoreListFindRequest request, Pageable pageable);

    Optional<Store> findOne(
            Long id, LocalDate checkInDate, LocalDate checkOutDate, int personCount);
}
