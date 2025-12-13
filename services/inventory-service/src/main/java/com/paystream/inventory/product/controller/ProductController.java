package com.paystream.inventory.product.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.service.ProductCreateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateService productCreateService;

    @PostMapping
    public BaseResponse<Long> create(
            HttpServletRequest request, @Valid @RequestBody ProductCreateRequest createRequest) {
        String userId = request.getHeader("X-Auth-User-Id");
        Long savedProductId = productCreateService.create(userId, createRequest);

        return BaseResponse.created(savedProductId);
    }
}
