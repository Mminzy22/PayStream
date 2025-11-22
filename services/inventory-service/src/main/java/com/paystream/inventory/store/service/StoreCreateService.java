package com.paystream.inventory.store.service;

import static com.paystream.core.exception.ExceptionEnum.STORE_ALREADY_EXISTS;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.store.dto.request.StoreCreateRequest;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreCreateService {

    private final StoreRepository storeRepository;

    @Transactional
    public Long create(StoreCreateRequest request) {
        Store store = request.toEntity();

        // 1. 호스트ID가 실제 존재하는 회원인지 확인

        // 2. 동일한 이름의 가게가 이미 등록되어 있는지 확인
        Boolean existsName = storeRepository.existsByName(store.getName());
        if (existsName) {
            throw new PayStreamException(STORE_ALREADY_EXISTS);
        }

        return storeRepository.save(store).getId();
    }
}
