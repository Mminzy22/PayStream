package com.paystream.inventory.product.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.dto.request.ProductUpdateRequest;
import com.paystream.inventory.product.service.ProductCreateService;
import com.paystream.inventory.product.service.ProductUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateService productCreateService;
    private final ProductUpdateService productUpdateService;

    @PostMapping
    public BaseResponse<Long> create(
            HttpServletRequest request, @Valid @RequestBody ProductCreateRequest createRequest) {
        String hostId = request.getHeader("X-Auth-User-Id");
        Long savedProductId = productCreateService.create(hostId, createRequest);

        return BaseResponse.created(savedProductId);
    }

    @PutMapping("/{productId}")
    public BaseResponse<Long> update(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Valid @RequestBody ProductUpdateRequest updateRequest) {
        String hostId = request.getHeader("X-Auth-User-Id");
        Long updatedProductId = productUpdateService.update(hostId, productId, updateRequest);

        return BaseResponse.created(updatedProductId);
    }
}
