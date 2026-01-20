package com.paystream.inventory.store.service;

import static com.paystream.core.exception.ExceptionEnum.STORE_ACCESS_DENIED;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.store.dto.request.StoreUpdateRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreUpdateService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse update(Long id, String hostId, StoreUpdateRequest request) {
        // hostId를 통해 해당 가게의 소유주가 맞는지 확인
        Store store = storeRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        // 소유주가 아닌 경우 예외를 발생
        if (!store.getHostId().equals(hostId)) {
            throw new PayStreamException(STORE_ACCESS_DENIED);
        }

        // 확인을 통해 맞다면 수정사항들을 update
        store.update(request);

        return StoreResponse.of(store);
    }
}
