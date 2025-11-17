package com.example.inventory.store.service;

import com.example.inventory.store.dto.request.StoreDeleteRequest;
import com.example.inventory.store.entity.Store;
import com.example.inventory.store.repository.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreDeleteService {

    private final StoreRepository storeRepository;

    public void deleted(StoreDeleteRequest request) {
        // 존재하는 hostId인지 확인

        // 삭제하고자 하는 가게가 존재하는지 확인
        List<Store> existedStore = storeRepository.findByIdIn(request.getStoreIds());

        if (existedStore.isEmpty()) {
            throw new IllegalArgumentException(
                    "Store with ids " + request.getStoreIds() + " does not exist");
        }

        // 조회된 가게들의 host가 요청한 host가 맞는지 확인
        boolean isHostExclusive =
                existedStore.stream()
                        .allMatch(store -> store.getHostId().equals(request.getHostId()));
        if (!isHostExclusive) {
            throw new IllegalArgumentException("가게 주인이 맞는지 다시 확인해주세요.");
        }

        // 조회된 가게들 중 요청한 가게가 포함되어있지 않는지 확인
        if (request.getStoreIds().size() != existedStore.size()) {
            throw new IllegalArgumentException("삭제가 불가능한 가게가 있습니다 다시 확인해주세요.");
        }

        // 확인이 되었다면 삭제
        storeRepository.deleteAllById(request.getStoreIds());
    }
}
