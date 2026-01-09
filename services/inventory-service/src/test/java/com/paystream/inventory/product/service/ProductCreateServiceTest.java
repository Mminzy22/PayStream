package com.paystream.inventory.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import com.paystream.inventory.utils.ImageUtils;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ProductCreateServiceTest {

    @Autowired private ProductCreateService productCreateService;

    @Autowired private StoreRepository storeRepository;

    @Autowired private ProductRepository productRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    private String hostId = "1";

    private Store savedStore;

    @MockitoBean private ImageUtils imageUtils;

    private MockMultipartFile file =
            new MockMultipartFile(
                    "file", // API에서 받을 파라미터 이름 (@RequestParam("file")
                    "test_image.png", // 원본 파일명 (filename)
                    "image/png", // 파일 타입 (contentType)
                    "test content".getBytes() // 파일 내용 (byte[])
                    );

    @BeforeEach
    void setUp() {
        Store store =
                Store.builder()
                        .name("Test Store")
                        .description("Test Store description")
                        .hostId("1")
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .category(Category.HOTEL)
                        .build();

        this.savedStore = storeRepository.save(store);
    }

    @Order(value = 1)
    @DisplayName("[성공] 상품 생성")
    @Test
    void testCreateProduct() throws IOException {
        // given
        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("name", "test_image.png");
        imageFile.put("path", "/test/path/test_image.png");

        given(imageUtils.saveImage(any())).willReturn(imageFile);

        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(5)
                        .build();

        // when
        Long savedProductId =
                productCreateService.create(hostId, request, Collections.singletonList(file));

        // then
        assertThat(savedProductId).isNotNull();
    }

    @DisplayName("[실패] 상품 생성 시 가게 주인이 다르면 예외가 발생한다")
    @Test
    void testCreateProductThrowNotStoreHost() throws IOException {
        // given
        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("name", "test_image.png");
        imageFile.put("path", "/test/path/test_image.png");

        given(imageUtils.saveImage(any())).willReturn(imageFile);

        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(5)
                        .build();

        String hostId = "2";

        // when
        // then
        assertThatThrownBy(
                        () ->
                                productCreateService.create(
                                        hostId, request, Collections.singletonList(file)))
                .isInstanceOf(PayStreamException.class)
                .hasMessage("해당 가게의 HostId와 다릅니다.");
    }

    @DisplayName("[실패] 상품 생성 시 가게 내에서 상품명이 겹쳐서 예외가 발생한다")
    @Test
    void testCreateProductThrowDuplicateProductName() throws IOException {
        // given
        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("name", "test_image.png");
        imageFile.put("path", "/test/path/test_image.png");

        given(imageUtils.saveImage(any())).willReturn(imageFile);

        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(5)
                        .build();

        // when
        productCreateService.create(hostId, request, Collections.singletonList(file));

        // then
        assertThatThrownBy(
                        () ->
                                productCreateService.create(
                                        hostId, request, Collections.singletonList(file)))
                .isInstanceOf(PayStreamException.class)
                .hasMessage("이미 존재하는 상품입니다.");
    }

    @DisplayName("[성공] 상품 생성 시 다른 가게의 상품명과 같아도 상품이 생성된다")
    @Test
    void testCreateProductAnotherStoreWithProductName() throws IOException {
        // given
        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("name", "test_image.png");
        imageFile.put("path", "/test/path/test_image.png");

        given(imageUtils.saveImage(any())).willReturn(imageFile);

        Store store =
                Store.builder()
                        .name("Test Store")
                        .description("Test Store description")
                        .hostId("1")
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .category(Category.HOTEL)
                        .build();
        store.addProduct(
                Product.builder()
                        .name("Test Product")
                        .description("Another Test Product description")
                        .build());
        storeRepository.save(store);

        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(5)
                        .build();

        // when
        Long savedProductId =
                productCreateService.create(hostId, request, Collections.singletonList(file));
        Product product = productRepository.findById(savedProductId).orElse(null);

        // then
        assertThat(product)
                .isNotNull()
                .extracting("name", "description")
                .containsExactlyInAnyOrder("Test Product", "Test Product description");
    }

    @DisplayName("[성공] 상품 생성 시 상품에 대한 재고가 생성된다.")
    @Test
    void testCreateProductWithCreateDailyInventory() throws IOException {
        // given
        Map<String, String> imageFile = new HashMap<>();
        imageFile.put("name", "test_image.png");
        imageFile.put("path", "/test/path/test_image.png");

        given(imageUtils.saveImage(any())).willReturn(imageFile);

        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .stock(5)
                        .build();

        Long savedProductId =
                productCreateService.create(hostId, request, Collections.singletonList(file));

        // when
        boolean isProductStock = dailyInventoryRepository.existsByProductId(savedProductId);
        List<DailyInventory> findProductDailyInventories =
                dailyInventoryRepository.findByProductId(savedProductId);

        // then
        assertThat(isProductStock).isTrue();
        assertThat(findProductDailyInventories).isNotNull().hasSize(30);
    }
}
