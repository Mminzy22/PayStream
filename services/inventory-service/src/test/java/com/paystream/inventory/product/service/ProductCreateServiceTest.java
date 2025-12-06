package com.paystream.inventory.product.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ProductCreateServiceTest {

    @Autowired private ProductCreateService productCreateService;

    @DisplayName("[성공] 상품 등록")
    @Test
    void createProduct() {
        // given

        // when

        // then
    }
}
