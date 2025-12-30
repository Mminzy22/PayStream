package com.paystream.inventory.store.repository;

import static com.paystream.inventory.store.entity.QStore.store;
import static com.paystream.inventory.store.repository.StorePredicate.*;

import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.entity.QStore;
import com.paystream.inventory.store.entity.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    public Optional<Store> findOne(
            Long id, LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        Store store =
                jpaQueryFactory
                        .selectFrom(QStore.store)
                        .leftJoin(QStore.store.amenities)
                        .fetchJoin()
                        .where(QStore.store.id.eq(id))
                        .fetchOne();

        return Optional.ofNullable(store);
    }
}
