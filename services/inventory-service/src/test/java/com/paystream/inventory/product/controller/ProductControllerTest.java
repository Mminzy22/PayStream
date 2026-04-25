package com.paystream.inventory.product.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired private ProductControllerImpl controller;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;

    @Autowired private StoreRepository storeRepository;

    @Value("${file.upload-dir}")
    private String basePath;

    @AfterEach
    void cleanup() throws IOException {
        Path path = Paths.get(basePath);
        if (Files.exists(path)) {
            // 폴더 내부의 모든 파일 삭제 후 폴더 삭제
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @DisplayName("[성공] 상품 생성 컨트롤러")
    @Test
    void productCreateTest() throws Exception {
        // given
        String hostId = "host-123";

        // 가게 생성
        Store store =
                Store.builder()
                        .hostId(hostId)
                        .name("Test Store")
                        .description("Test Store")
                        .address(new Address("test", "test"))
                        .category(Category.HOTEL)
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .basePersonCount(2)
                        .build();

        Store savedStore = storeRepository.save(store);

        // 상품 생성
        ProductCreateRequest createRequest =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("테스트 상품")
                        .description("테스트 설명")
                        .basePrice(1000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(3)
                        .build();
        String dtoString = objectMapper.writeValueAsString(createRequest);

        MockMultipartFile requestPart =
                new MockMultipartFile(
                        "createRequest",
                        "",
                        "application/json",
                        dtoString.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile filePart =
                new MockMultipartFile(
                        "file",
                        "test.png",
                        MediaType.IMAGE_PNG_VALUE,
                        "test image content".getBytes());

        // when
        // then
        mockMvc.perform(
                        multipart("/products")
                                .file(requestPart) // JSON 데이터 추가
                                .file(filePart) // 이미지 파일 추가
                                .header("X-Auth-User-Id", hostId)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CREATED"));
    }
}
