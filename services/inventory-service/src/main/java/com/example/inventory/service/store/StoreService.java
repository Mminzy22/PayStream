package com.example.inventory.service.store;

import com.example.inventory.dto.StoreDto;
import com.example.inventory.entity.product.Photo;
import com.example.inventory.entity.store.Store;
import com.example.inventory.repository.store.StoreRepository;
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
        List<Store> stores = storeRepository.findAllByFetchJoin();

        return stores.stream()
                .map(store -> {
                    List<String> photos = store.getProducts().stream()
                            .findFirst() // 첫번째 상품
                            .map(product -> product.getPhotos().stream().map(Photo::getUrl).toList())
                            .orElse(Collections.emptyList());// 없으면 빈 리스트로 반환

                    return StoreDto.Response.builder()
                            .storeId(store.getId())
                            .hostId(store.getHostId())
                            .name(store.getName())
                            .description(store.getDescription())
                            .address(store.getAddress())
                            .category(store.getCategory().getName())
                            .checkInTime(store.getCheckInTime())
                            .checkOutTime(store.getCheckOutTime())
                            .amenities(store.getAmenities())
                            .photos(photos)
                            .rules(store.getRules())
                            .rating(store.getRating())
                            .reviewCount(store.getReviewCount())
                            .build();
                })
                .toList();
    }

    public UUID create(StoreDto.Request request) {
        // 실제 있는 호스트(유저) 인지 확인하는 로직
        // 코드 --

        Store store = request.toEntity();
        UUID uuid = storeRepository.save(store).getId();

        return uuid;
    }

}
