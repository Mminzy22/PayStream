package com.paystream.inventory.inventory.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.annotation.DistributedLock;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockManagerService {

    private final DailyInventoryRepository dailyInventoryRepository;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    /**
     * 분산락 정리
     *
     * <p>분산 락은 트랜잭션을 잘 써야한다. 락 획득 및 반납은 트랜잭션 바깥에서 진행되어야 하고, 비즈니스 로직은 트랜잭션 내에서 동작해야 한다. 이유는 비즈니스 로직이
     * 수행 된 후 트랜잭션이 종료되지 않고 락을 반납하게 되면 다음 요청이 락을 획득하게 되는데, 이전 작업이 커밋되지 않았기 때문에 데이터 갱신 오류가 발생하게 되면서
     * 데이터 정합성이 깨질 수 있다. 따라서 비즈니스 로직이 커밋까지 완료된 후 락을 반납해야 이러한 문제가 발생하지 않는다.
     */

    /**
     * 숙소 예약이 들어오면 해당 상품의 재고를 감소 시킨다.
     *
     * @param productId 상품 아이디
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     */
    @DistributedLock(key = "'lock:inventory:' + #productId")
    public void decreaseStock(Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        // 날짜 역전 확인
        if (checkInDate.isAfter(checkOutDate)) {
            throw new PayStreamException(INVALID_DATE_RANGE);
        }

        List<DailyInventory> inventoryList =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, checkInDate, checkOutDate);

        // 조회된 일수와 기대 일수 확인
        long expectedDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (expectedDays != inventoryList.size()) {
            log.error(
                    "재고 데이터 불일치: 상품ID={}, 기대일수={}, 조회된일수={}",
                    productId,
                    expectedDays,
                    inventoryList.size());
            throw new PayStreamException(OUT_OF_BOOKING_PERIOD);
        }

        // 모든 날짜에 재고가 있는지 확인
        boolean isStockAvailable =
                inventoryList.stream().allMatch(DailyInventory::isStockAvailable);

        // 재고 감소
        if (inventoryList.isEmpty() || !isStockAvailable) {
            throw new PayStreamException(INSUFFICIENT_STOCK);
        }

        inventoryList.forEach(DailyInventory::decreaseStockAvailable);
    }

    /**
     * 숙소 예약이 취소되면 재고를 증가 시킨다. (단, 기본 설정된 재고보다 많아질 순 없다.)
     *
     * @param productId 상품 아이디
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     */
    @DistributedLock(key = "'lock:inventory:' + #productId")
    public void increaseStock(Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<DailyInventory> inventoryList =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, checkInDate, checkOutDate);

        long expectedDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (expectedDays != inventoryList.size()) {
            throw new PayStreamException(INVALID_STOCK_CANCEL_REQUEST);
        }

        inventoryList.forEach(DailyInventory::increaseStockAvailable);
    }
}
