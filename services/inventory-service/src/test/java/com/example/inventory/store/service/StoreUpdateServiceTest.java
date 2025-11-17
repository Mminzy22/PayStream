package com.example.inventory.store.service;

import static com.example.inventory.store.entity.Amenities.BREAKFAST_INCLUDED;
import static com.example.inventory.store.entity.Amenities.PARKING;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.store.dto.request.StoreUpdateRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import com.example.inventory.store.repository.StoreRepository;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class StoreUpdateServiceTest {

    @Autowired private StoreUpdateService storeUpdateService;

    @Autowired private StoreRepository storeRepository;

    @DisplayName("가게 수정 사항으로 체크인과 체크아웃 시간만 변경")
    @Test
    void storeUpdateWithCheckInAndCheckOut() {
        // given
        Store foundStore =
                createStore("1", "한강 뷰 맛집", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);

        Store savedStore = storeRepository.save(foundStore);

        Long updateId = savedStore.getId();
        StoreUpdateRequest request =
                StoreUpdateRequest.builder()
                        .hostId("1")
                        .checkInTime(LocalTime.of(15, 0))
                        .checkOutTime(LocalTime.of(10, 0))
                        .build();

        // when
        StoreResponse response = storeUpdateService.update(updateId, request);

        // then
        assertThat(response)
                .isNotNull()
                .extracting("checkInTime", "checkOutTime")
                .containsExactly(LocalTime.of(15, 0), LocalTime.of(10, 0));
    }

    private Store createStore(
            String hostId, String name, List<Amenities> amenities, Category category) {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .build();
    }
}
