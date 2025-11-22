package com.paystream.inventory.store.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.store.dto.request.StoreDeleteRequest;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
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
            throw new PayStreamException(STORE_NOT_FOUND);
        }

        // 조회된 가게들의 host가 요청한 host가 맞는지 확인
        boolean isHostExclusive =
                existedStore.stream()
                        .allMatch(store -> store.getHostId().equals(request.getHostId()));
        if (!isHostExclusive) {
            throw new PayStreamException(STORE_ACCESS_DENIED);
        }

        // 조회된 가게들 중 요청한 가게가 포함되어있지 않는지 확인
        if (request.getStoreIds().size() != existedStore.size()) {
            throw new PayStreamException(STORE_DELETION_BLOCKED);
        }

        // 확인이 되었다면 삭제
        storeRepository.deleteAllById(request.getStoreIds());
    }
}
