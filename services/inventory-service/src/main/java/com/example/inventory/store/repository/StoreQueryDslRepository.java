package com.example.inventory.store.repository;

import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreQueryDslRepository {

    Page<Store> findAllByFetchJoin(StoreUserFindRequest request, Pageable pageable);

}
