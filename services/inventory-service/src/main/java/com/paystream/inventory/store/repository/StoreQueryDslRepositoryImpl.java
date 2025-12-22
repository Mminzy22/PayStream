package com.paystream.inventory.store.repository;

import static com.paystream.inventory.inventory.entity.QDailyInventory.dailyInventory;
import static com.paystream.inventory.product.entity.QProduct.product;
import static com.paystream.inventory.store.entity.QStore.store;
import static com.paystream.inventory.store.repository.StorePredicate.*;

import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.entity.QStore;
import com.paystream.inventory.store.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class StoreQueryDslRepositoryImpl implements StoreQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public Page<Store> findAllByFetchJoin(StoreListFindRequest request, Pageable pageable) {
        List<Store> stores =
                jpaQueryFactory
                        .select(store)
                        .from(store)
                        .leftJoin(store.amenities)
                        .fetchJoin()
                        .where(
                                nameLike(request.getName()),
                                categoryEqual(request.getCategory()),
                                addressEqual(request.getProvince(), request.getCity()),
                                amenitiesAllMatch(request.getAmenities()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .distinct()
                        .fetch();

        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        List<Product> products =
                getProducts(
                        storeIds,
                        request.getCheckInDate(),
                        request.getCheckOutDate(),
                        request.getPersonCount());

        List<Store> finalStores = mapProductsToStores(stores, products);

        Long total =
                jpaQueryFactory
                        .select(store.countDistinct()) // DISTINCT를 포함한 COUNT
                        .from(store)
                        .where(
                                nameLike(request.getName()),
                                categoryEqual(request.getCategory()),
                                addressEqual(request.getProvince(), request.getCity()),
                                amenitiesAllMatch(request.getAmenities()))
                        .fetchOne();

        return new PageImpl<>(finalStores, pageable, Optional.ofNullable(total).orElse(0L));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Store> findOne(
            Long id, LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        Store store =
                jpaQueryFactory
                        .selectFrom(QStore.store)
                        .leftJoin(QStore.store.amenities)
                        .fetchJoin()
                        .where(QStore.store.id.eq(id))
                        .fetchOne();

        if (store == null) {
            return Optional.empty();
        }

        List<Product> products = getProducts(store.getId(), checkInDate, checkOutDate, personCount);

        Store finalStore = mapProductsToStores(store, products);

        return Optional.ofNullable(finalStore);
    }

    private List<Product> getProducts(
            Long storeId, LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        return getProducts(
                Collections.singletonList(storeId), checkInDate, checkOutDate, personCount);
    }

    private List<Product> getProducts(
            List<Long> storeIds, LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        validateStoreIds(storeIds);

        List<Long> insufficientIds =
                getAndValidateInsufficientProductIds(checkInDate, checkOutDate);

        return jpaQueryFactory
                .selectDistinct(product)
                .from(product)
                .where(
                        product.store.id.in(storeIds),
                        product.maxPersonCount.goe(personCount),
                        product.id.notIn(insufficientIds))
                .fetch()
                .stream()
                .filter(
                        p ->
                                !p.getDailyInventories()
                                        .isEmpty()) // 현재 상품을 조회시 재고가 없는 상품들도 조회되어 따로 filter 처리 진행
                .toList();
    }

    private void validateStoreIds(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            throw new IllegalArgumentException("조회할 Store ID가 없습니다.");
        }
    }

    private List<Long> getAndValidateInsufficientProductIds(
            LocalDate checkInDate, LocalDate checkOutDate) {
        return jpaQueryFactory
                .select(dailyInventory.product.id)
                .from(dailyInventory)
                .where(
                        dailyInventory.date.between(checkInDate, checkOutDate),
                        dailyInventory.stockAvailable.loe(0))
                .distinct()
                .fetch();
    }

    private Store mapProductsToStores(Store store, List<Product> products) {
        List<Store> resultList = mapProductsToStores(Collections.singletonList(store), products);

        if (resultList.isEmpty()) {
            return null;
        }

        return resultList.get(0);
    }

    private List<Store> mapProductsToStores(List<Store> stores, List<Product> products) {
        Map<Long, List<Product>> productMap =
                products.stream().collect(Collectors.groupingBy(p -> p.getStore().getId()));

        for (Store store : stores) {
            List<Product> productList = productMap.get(store.getId());

            store.getProducts().clear(); // 기존에 있던 상품들은 제거 하고 재고가 있는 상품들만 List에 삽입
            if (productList != null) {
                store.getProducts().addAll(productList);
            }
        }

        // products가 하나도 없으면 해당 store는 List에서 제거
        return stores.stream().filter(store -> !store.getProducts().isEmpty()).toList();
    }
}
