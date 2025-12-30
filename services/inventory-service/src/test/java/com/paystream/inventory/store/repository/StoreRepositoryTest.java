package com.paystream.inventory.store.repository;

import static com.paystream.inventory.store.entity.Amenities.*;
import static com.paystream.inventory.store.entity.Amenities.BAR_LOUNGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import jakarta.transaction.Transactional;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class StoreRepositoryTest {

    @Autowired private StoreRepository storeRepository;

    @DisplayName("가게 전체 조회시 부대시설도 함께 조회된다.")
    @Test
    void testFindAllWithPaging() {
        // given
        Store store1 =
                Store.builder()
                        .hostId("1")
                        .name("testStore1")
                        .category(Category.HOTEL)
                        .address(new Address("test", "test"))
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .basePersonCount(2)
                        .amenities(List.of(PARKING, BREAKFAST_INCLUDED, RESTAURANT))
                        .build();
        Store store2 =
                Store.builder()
                        .hostId("1")
                        .name("testStore2")
                        .category(Category.PENSION)
                        .address(new Address("test", "test"))
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .basePersonCount(2)
                        .amenities(List.of(BAR_LOUNGE))
                        .build();

        storeRepository.saveAll(List.of(store1, store2));

        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<Store> result = storeRepository.findAllWithPaging(pageable);

        // then
        assertThat(result.getContent())
                .hasSize(2)
                .usingRecursiveComparison() // 재귀적 비교
                .ignoringCollectionOrder() // 컬렉션 내부의 순서는 무시하고 내용만 비교
                .ignoringFields("id", "createdAt", "updatedAt") // 생성 시점마다 달라지는 필드는 제외
                .isEqualTo(List.of(store1, store2));
    }
}
