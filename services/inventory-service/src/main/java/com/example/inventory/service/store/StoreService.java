package com.example.inventory.service.store;

import com.example.inventory.dto.request.StoreRequest;
import com.example.inventory.dto.response.StoreResponse;
import com.example.inventory.entity.store.Store;
import com.example.inventory.repository.store.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<StoreResponse> findAll() {
        List<Store> stores = storeRepository.findAllByFetchJoin().orElse(Collections.emptyList());

        // 연관 관계 매핑으로 데이터를 가져오려면 @Transactional이 있어야한다.
        return stores.stream()
                .map(StoreResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreResponse findById(String id) {
        UUID uuid = UUID.fromString(id);

        Store store = storeRepository.findByIdByFetchJoin(uuid)
                .orElseThrow(EntityNotFoundException::new);

        return StoreResponse.of(store);
    }

    public UUID create(StoreRequest request) {
        // 실제 있는 호스트(유저) 인지 확인하는 로직
        // 코드 --

        Store store = request.toEntity();
        UUID uuid = storeRepository.save(store).getId();

        return uuid;
    }

    public StoreResponse update(String id, StoreRequest request) {
        UUID uuid = UUID.fromString(id);

        Store store = storeRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);

        store.update(request);
        Store updateStore = storeRepository.save(store);

        return StoreResponse.of(updateStore);
    }

    public void delete(String id) {
        UUID uuid = UUID.fromString(id);
        Store store = storeRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);
        storeRepository.delete(store);
    }

}
