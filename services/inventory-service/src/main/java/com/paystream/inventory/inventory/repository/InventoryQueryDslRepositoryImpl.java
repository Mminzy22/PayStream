package com.paystream.inventory.inventory.repository;

import static com.paystream.inventory.inventory.entity.QDailyInventory.dailyInventory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryQueryDslRepositoryImpl implements InventoryQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long inventoriesDecreaseBulk(
            Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        return jpaQueryFactory
                .update(dailyInventory)
                .set(
                        dailyInventory.stockAvailable,
                        dailyInventory.stockAvailable.subtract(1)) // subtract: 빼기 연산
                .where(
                        dailyInventory
                                .product
                                .id
                                .eq(productId)
                                .and(dailyInventory.date.goe(checkInDate))
                                .and(dailyInventory.date.lt(checkOutDate))
                                .and(dailyInventory.stockAvailable.gt(0)))
                .execute();
    }

    @Override
    public long inventoriesIncreaseBulk(
            Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        return jpaQueryFactory
                .update(dailyInventory)
                .set(
                        dailyInventory.stockAvailable,
                        dailyInventory.stockAvailable.add(1)) // subtract: 빼기 연산
                .where(
                        dailyInventory
                                .product
                                .id
                                .eq(productId)
                                .and(dailyInventory.date.goe(checkInDate))
                                .and(dailyInventory.date.lt(checkOutDate))
                                .and(dailyInventory.stockAvailable.gt(0)))
                .execute();
    }
}
