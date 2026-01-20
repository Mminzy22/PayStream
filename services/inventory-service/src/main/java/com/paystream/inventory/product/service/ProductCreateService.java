package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.NOT_STORE_HOST;
import static com.paystream.core.exception.ExceptionEnum.STORE_NOT_FOUND;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.photo.entity.Photo;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import com.paystream.inventory.utils.ImageUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ProductCreateService {

    private final ImageUtils imageUtils;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    public Long create(String hostId, ProductCreateRequest request, List<MultipartFile> files) {
        Store findStore = findStore(hostId, request);

        validateProductName(request, findStore);

        List<Map<String, String>> uploadsFile = savedImageAndGetPath(files);

        Product savedProduct = getSavedProduct(request, findStore);
        productEnterPhotos(uploadsFile, savedProduct);

        fillDailyStock(savedProduct, 30);

        return savedProduct.getId();
    }

    // 상품에 이미지들을 저장
    private void productEnterPhotos(List<Map<String, String>> uploadsFile, Product savedProduct) {
        List<Photo> photos =
                uploadsFile.stream()
                        .map(
                                file -> {
                                    return Photo.builder()
                                            .fileName(file.get("name"))
                                            .imagePath(file.get("path"))
                                            .product(savedProduct)
                                            .build();
                                })
                        .toList();
        savedProduct.getPhotos().addAll(photos);
    }

    // 이미지 저장 후 저장경로 반환
    private List<Map<String, String>> savedImageAndGetPath(List<MultipartFile> files) {
        return files.parallelStream()
                .map(
                        file -> {
                            try {
                                return imageUtils.saveImage(file);
                            } catch (Exception e) {
                                // 실패 시 로깅을 하거나 특정 결과값(null 등)을 반환
                                log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
                                return null;
                            }
                        })
                .filter(Objects::nonNull) // 성공한 경로만 수집
                .toList();
    }

    // 상품 생성 시 daysToAdd 만큼 날짜별 재고를 생성
    private void fillDailyStock(Product savedProduct, int daysToAdd) {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(daysToAdd);

        List<DailyInventory> dailyInventories =
                today.datesUntil(until)
                        .map(
                                date ->
                                        DailyInventory.builder()
                                                .product(savedProduct)
                                                .date(date)
                                                .stockAvailable(savedProduct.getBaseStock())
                                                .build())
                        .toList();

        dailyInventoryRepository.saveAll(dailyInventories);
    }

    private Product getSavedProduct(ProductCreateRequest request, Store findStore) {
        Product product = request.toEntity();
        product.assignStore(findStore);

        return productRepository.save(product);
    }

    // 가게 내에 동일한 상품명이 있는지 검증
    private void validateProductName(ProductCreateRequest request, Store findStore) {
        boolean isProductName =
                productRepository.existsByStoreIdAndName(findStore.getId(), request.getName());

        if (isProductName) {
            throw new PayStreamException(ExceptionEnum.PRODUCT_ALREADY_EXISTS);
        }
    }

    private Store findStore(String hostId, ProductCreateRequest request) {
        Store findStore =
                storeRepository
                        .findById(request.getStoreId())
                        .orElseThrow(() -> new PayStreamException(STORE_NOT_FOUND));

        if (!findStore.getHostId().equals(hostId)) {
            throw new PayStreamException(NOT_STORE_HOST);
        }

        return findStore;
    }
}
