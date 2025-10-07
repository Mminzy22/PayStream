package com.example.inventory.service.store;

import com.example.inventory.dto.StoreDto;
import com.example.inventory.entity.product.Photo;
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
    public List<StoreDto.Response> findAll() {
        List<Store> stores = storeRepository.findAllByFetchJoin().orElse(Collections.emptyList());

        // 연관 관계 매핑으로 데이터를 가져오려면 @Transactional이 있어야한다.
        return stores.stream()
                .map(StoreDto.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreDto.Response findById(String id) {
        UUID uuid = UUID.fromString(id);

        Store store = storeRepository.findByIdByFetchJoin(uuid)
                .orElseThrow(EntityNotFoundException::new);

        return StoreDto.Response.from(store);
    }

    public UUID create(StoreDto.Request request) {
        // 실제 있는 호스트(유저) 인지 확인하는 로직
        // 코드 --

        Store store = request.toEntity();
        UUID uuid = storeRepository.save(store).getId();

        return uuid;
    }

    public StoreDto.Response update(String id, StoreDto.Request request) {
        UUID uuid = UUID.fromString(id);

        Store store = storeRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);

        store.update(request);
        Store updateStore = storeRepository.save(store);

        return StoreDto.Response.from(updateStore);
    }

    public void delete(String id) {
        UUID uuid = UUID.fromString(id);
        Store store = storeRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);
        storeRepository.delete(store);
    }

}
