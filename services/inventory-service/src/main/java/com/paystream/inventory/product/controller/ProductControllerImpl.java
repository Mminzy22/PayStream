package com.paystream.inventory.product.controller;

import static com.paystream.inventory.utils.AuthUtils.getCurrentUserId;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.dto.request.ProductDeleteRequest;
import com.paystream.inventory.product.dto.request.ProductUpdateRequest;
import com.paystream.inventory.product.dto.response.ProductDetailResponse;
import com.paystream.inventory.product.dto.response.ProductResponse;
import com.paystream.inventory.product.service.ProductCreateService;
import com.paystream.inventory.product.service.ProductDeleteService;
import com.paystream.inventory.product.service.ProductFindService;
import com.paystream.inventory.product.service.ProductUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ProductFindService productFindService;
    private final ProductCreateService productCreateService;
    private final ProductUpdateService productUpdateService;
    private final ProductDeleteService productDeleteService;

    @GetMapping("/{productId}")
    @Override
    public BaseResponse<ProductDetailResponse> findById(@PathVariable Long productId) {
        ProductDetailResponse productDetail = productFindService.findProductDetail(productId);
        return BaseResponse.ok(productDetail);
    }

    @GetMapping(
            value = "/{productId}",
            params = {"checkInDate", "checkOutDate"})
    @Override
    public BaseResponse<ProductDetailResponse> findByIdAndStock(
            @PathVariable Long productId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        ProductDetailResponse productDetail =
                productFindService.findProductWithDailyInventory(
                        productId, checkInDate, checkOutDate);
        return BaseResponse.ok(productDetail);
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public BaseResponse<Long> create(
            HttpServletRequest request,
            @Valid @RequestPart("createRequest") ProductCreateRequest createRequest,
            @RequestPart("file") List<MultipartFile> files) {
        String hostId = getCurrentUserId(request);
        Long savedProductId = productCreateService.create(hostId, createRequest, files);

        return BaseResponse.created(savedProductId);
    }

    @PutMapping("/{productId}")
    @Override
    public BaseResponse<Long> update(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Valid @RequestBody ProductUpdateRequest updateRequest) {
        String hostId = getCurrentUserId(request);
        Long updatedProductId = productUpdateService.update(hostId, productId, updateRequest);

        return BaseResponse.created(updatedProductId);
    }

    @DeleteMapping("/{productId}")
    @Override
    public BaseResponse<String> delete(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Valid @RequestBody ProductDeleteRequest deleteRequest) {
        String hostId = getCurrentUserId(request);
        productDeleteService.delete(hostId, productId, deleteRequest);

        return BaseResponse.ok("성공적으로 삭제되었습니다.");
    }

    @GetMapping("/user/list")
    @Override
    public BaseResponse<List<ProductResponse>> findOwnedProductList(HttpServletRequest request) {
        String hostId = getCurrentUserId(request);

        List<ProductResponse> response = productFindService.listUserProducts(hostId);

        return BaseResponse.ok(response);
    }
}
