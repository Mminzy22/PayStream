package com.paystream.inventory.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class StockManagerServiceTest {

    @Autowired private StockManagerService stockManagerService;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @Autowired private StoreRepository storeRepository;

    @Autowired private RedissonClient redissonClient;

    @Autowired private ProductRepository productRepository;

    private Long productId;
    private static final LocalDate CHECK_IN_DATE = LocalDate.now();
    private static final LocalDate CHECK_OUT_DATE = LocalDate.now().plusDays(2);

    @BeforeEach
    void setUp() {
        // 가게 생성
        String hostId = "host-1";
        Store store =
                Store.builder()
                        .hostId(hostId)
                        .name("테스트 가게")
                        .address(new Address("테스트 지역", "테스트 도시"))
                        .category(Category.HOTEL)
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now())
                        .basePersonCount(2)
                        .build();

        // 상품 생성
        Product product =
                Product.builder()
                        .name("테스트 상품")
                        .description("테스트 설명")
                        .minPersonCount(2)
                        .maxPersonCount(2)
                        .basePrice(1000)
                        .personAddPrice(1000)
                        .baseStock(5)
                        .build();
        store.addProduct(product);

        for (int i = 0; i < 3; i++) {
            DailyInventory inventory =
                    DailyInventory.builder()
                            .date(LocalDate.now().plusDays(i))
                            .stockAvailable(5)
                            .build();
            product.addDailyInventory(inventory);
        }

        storeRepository.save(store);
        this.productId = product.getId();
    }

    @DisplayName("[성공] 상품의 재고가 5개 있을 때 요청하면 1개씩 소모된다.")
    @Test
    void consumeDecreaseStock() {
        // given
        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = LocalDate.now().plusDays(2);

        // when
        stockManagerService.decreaseStock(productId, checkInDate, checkOutDate);

        // then
        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, checkInDate, checkOutDate);
        assertThat(dailyInventories)
                .hasSize(2)
                .extracting("date", "stockAvailable")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LocalDate.now(), 4),
                        Tuple.tuple(LocalDate.now().plusDays(1), 4));
    }

    @DisplayName("[성공] 상품의 재고가 5개 있을 때 동시에 5명이 요청하면 모두 소모 된다.")
    @Test
    void consumeAllStock() throws InterruptedException {
        // given
        int threadCount = 5; // 동시에 들어오는 인원수
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(
                    () -> {
                        try {
                            stockManagerService.decreaseStock(
                                    productId, CHECK_IN_DATE, CHECK_OUT_DATE);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
        }
        countDownLatch.await();

        // then
        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, CHECK_IN_DATE, CHECK_OUT_DATE);
        assertThat(dailyInventories)
                .hasSize(2)
                .extracting("date", "stockAvailable")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LocalDate.now(), 0),
                        Tuple.tuple(LocalDate.now().plusDays(1), 0));
    }

    @DisplayName("[실패] 상품의 재고가 5개 있을 때 동시에 6명이 요청하면 예외가 발생한다.")
    @Test
    void throwExceptionWhenOutOfStock() throws InterruptedException {
        // given
        int threadCount = 6; // 동시에 들어오는 인원수
        int initialStock = 5; // 가정: 현재 모든 날짜의 재고가 5개임

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // 멀티쓰레드 환경에서 안전하게 정수(int)를 조작할 수 있게 해주는 클래스
        // 성공 횟수와 실패 횟수를 카운트
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(
                    () -> {
                        try {
                            stockManagerService.decreaseStock(
                                    productId, CHECK_IN_DATE, CHECK_OUT_DATE);
                            successCount.incrementAndGet();
                        } catch (PayStreamException e) {
                            if (e.getMessage().equals("재고가 부족한 날짜가 있습니다. 다시 확인해주세요.")) {
                                failCount.incrementAndGet();
                            }
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
        }
        countDownLatch.await(); // 모든 쓰레드가 일을 마칠 때까지 메인 쓰레드를 멈춰 세우는 정지 신호

        // then
        assertThat(successCount.get()).isEqualTo(5); // 5명은 성공
        assertThat(failCount.get()).isEqualTo(1); // 1명은 실패(예외 발생)

        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, CHECK_IN_DATE, CHECK_OUT_DATE);
        assertThat(dailyInventories).allMatch(inventory -> inventory.getStockAvailable() == 0);
    }

    @DisplayName("다른 쓰레드가 이미 락을 점유하고 있으면 락 획득 실패 예외가 발생한다.")
    @Test
    void throwExceptionWhenLockAcquisitionFails() {
        // given
        String lockKey = "lock:inventory:" + productId;
        RLock rLock = redissonClient.getLock(lockKey);

        // when
        // 다른 쓰레드를 만들고 락을 점유하도록 함.
        Thread lockPreemptor =
                new Thread(
                        () -> {
                            rLock.lock(); // 락 잡기
                            try {
                                Thread.sleep(13000); // 13초 대기
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                rLock.unlock();
                            }
                        });
        lockPreemptor.start();

        // then
        assertThatThrownBy(
                        () ->
                                stockManagerService.decreaseStock(
                                        productId, CHECK_IN_DATE, CHECK_OUT_DATE))
                .hasMessageContaining("시스템이 혼잡하여 취소 처리가 지연되고 있습니다.");
    }

    @DisplayName("예약 취소시 상품의 재고를 다시 채웁니다.")
    @Test
    void restoreStockWhenReservationIsCancelled() throws InterruptedException {
        // given
        List<DailyInventory> findInventories =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, CHECK_IN_DATE, CHECK_OUT_DATE);
        findInventories.forEach(DailyInventory::decreaseStockAvailable);
        dailyInventoryRepository.flush(); // 수정된 재고를 커밋

        int threadCount = 5; // 동시에 들어오는 인원수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(
                    () -> {
                        try {
                            stockManagerService.increaseStock(
                                    productId, CHECK_IN_DATE, CHECK_OUT_DATE);
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        latch.await();

        // then
        // 초기 재고 감소 확인용
        assertThat(findInventories)
                .hasSize(2)
                .extracting("date", "stockAvailable")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LocalDate.now(), 4),
                        Tuple.tuple(LocalDate.now().plusDays(1), 4));

        // 동시성 처리를 통해 증가된 상태의 인벤토리
        List<DailyInventory> inventories =
                dailyInventoryRepository.findInventoriesByDateRange(
                        productId, CHECK_IN_DATE, CHECK_OUT_DATE);
        assertThat(inventories)
                .hasSize(2)
                .extracting("date", "stockAvailable")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(LocalDate.now(), 5),
                        Tuple.tuple(LocalDate.now().plusDays(1), 5));
    }
}
