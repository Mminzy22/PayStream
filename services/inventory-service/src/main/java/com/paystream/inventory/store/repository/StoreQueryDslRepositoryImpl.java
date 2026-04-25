package com.paystream.inventory.store.repository;

import static com.paystream.inventory.inventory.entity.QDailyInventory.dailyInventory;
import static com.paystream.inventory.product.entity.QProduct.product;
import static com.paystream.inventory.store.entity.QStore.store;
import static com.paystream.inventory.store.repository.StorePredicate.*;

import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.entity.QStore;
import com.paystream.inventory.store.entity.Store;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    private final ProductRepository productRepository;

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
                                hostIdEqual(request.getOwnerId()),
                                nameLike(request.getName()),
                                categoryEqual(request.getCategory()),
                                addressEqual(request.getProvince(), request.getCity()),
                                amenitiesAllMatch(request.getAmenities()))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .distinct()
                        .fetch();

        List<Long> storeIds = stores.stream().map(Store::getId).toList();

        Map<Long, List<Product>> productsMap =
                getProductsMap(
                        storeIds,
                        request.getPersonCount(),
                        request.getCheckInDate(),
                        request.getCheckOutDate());

        // 가게에 상품을 세팅
        for (Store store : stores) {
            List<Product> products =
                    productsMap.getOrDefault(store.getId(), Collections.emptyList());
            store.getProducts().clear(); // 가게에서 조회된 상품들을 초기화 (Cartesian Product 문제)
            store.getProducts().addAll(products);
        }

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

        return new PageImpl<>(stores, pageable, Optional.ofNullable(total).orElse(0L));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Store> findOne(Long id, LocalDate checkInDate, LocalDate checkOutDate) {
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

        Map<Long, List<Product>> productsMap =
                getProductsMap(store.getId(), checkInDate, checkOutDate);

        // 가게에 상품을 세팅
        List<Product> findProducts =
                productsMap.getOrDefault(store.getId(), Collections.emptyList());
        store.getProducts().clear();
        store.getProducts().addAll(findProducts);

        return Optional.of(store);
    }

    // 최대인원수 상관없이 상품을 조회하고 싶을 때 사용
    private Map<Long, List<Product>> getProductsMap(
            Long id, LocalDate checkInDate, LocalDate checkOutDate) {
        return getProductsMap(Collections.singletonList(id), null, checkInDate, checkOutDate);
    }

    // 카티시안 곱으로 인해 가게의 상품들을 재조회 후 가게에 삽입
    private Map<Long, List<Product>> getProductsMap(
            List<Long> storeIds,
            Integer personCount,
            LocalDate checkInDate,
            LocalDate checkOutDate) {
        return jpaQueryFactory
                .selectFrom(product)
                .where(
                        personCountGoe(personCount),
                        product.store.id.in(storeIds),
                        product.id.in(subQueryDailyInventories(checkInDate, checkOutDate)))
                .distinct()
                .stream()
                .collect(Collectors.groupingBy(p -> p.getStore().getId()));
    }

    private BooleanExpression personCountGoe(Integer personCount) {
        // 값이 null이면 null을 반환 -> where 절에서 null은 자동으로 무시됨
        return personCount != null ? product.maxPersonCount.goe(personCount) : null;
    }

    private List<Long> subQueryDailyInventories(LocalDate checkInDate, LocalDate checkOutDate) {
        return jpaQueryFactory
                .select(dailyInventory.product.id)
                .from(dailyInventory)
                .where(dailyInventory.date.goe(checkInDate), dailyInventory.date.lt(checkOutDate))
                .distinct()
                .fetch();
    }
}
