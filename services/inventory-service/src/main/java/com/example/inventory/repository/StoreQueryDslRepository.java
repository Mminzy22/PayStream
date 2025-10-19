package com.example.inventory.repository;

import com.example.inventory.dto.store.request.StoreUserFindRequest;
import com.example.inventory.entity.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreQueryDslRepository {

    Page<Store> findAllByFetchJoin(StoreUserFindRequest request, Pageable pageable);

}
