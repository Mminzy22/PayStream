package com.paystream.inventory.scheduler;

import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 매일 새벽 2시에 날짜별 재고를 확인하고 30개가 안되는 재고는 1개 추가하는 스케줄러 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryProvisioningScheduler {

    private final DailyInventoryRepository dailyInventoryRepository;
    private static final int THIRTY_DAYS = 30;

    // 사람이 몰리지 않으면서 이용객이 적을 시간에 스케줄러를 실행시켜 에러를 최소화 함.
    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduled() {
        // 저장된 상품의 재고 중 최신 날짜만 조회
        List<DailyInventory> latestInventories =
                dailyInventoryRepository.findLatestInventoriesNative();
        List<DailyInventory> newDateInventory = new ArrayList<>();
        LocalDate targetDate = LocalDate.now().plusDays(THIRTY_DAYS - 1);

        // 재고의 최신 날짜가 현재로부터 30일 차이가 나는지 확인, 30일보다 작은것은 추출
        latestInventories.stream()
                .filter(
                        inventory -> {
                            return inventory.getDate().isBefore(targetDate);
                        })
                .forEach(
                        latest -> {
                            // 부족한 날짜만큼 반복해서 생성
                            LocalDate startDate = latest.getDate().plusDays(1);

                            // 마지막 날짜 다음날부터 targetDate까지 순회하며 리스트에 저장
                            while (!startDate.isAfter(targetDate)) {
                                DailyInventory newInventory =
                                        DailyInventory.builder()
                                                .date(startDate)
                                                .product(latest.getProduct())
                                                .stockAvailable(latest.getProduct().getBaseStock())
                                                .build();

                                newDateInventory.add(newInventory);
                                startDate = startDate.plusDays(1);
                            }
                        });

        if (!newDateInventory.isEmpty()) {
            dailyInventoryRepository.saveAll(newDateInventory);
            log.info("총 {}건의 신규 재고 데이터가 생성되었습니다.", newDateInventory.size());
        } else {
            log.info("생성할 신규 재고 데이터가 없습니다.");
        }
    }
}
