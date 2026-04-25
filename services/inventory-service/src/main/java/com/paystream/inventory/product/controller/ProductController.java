package com.paystream.inventory.product.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.dto.request.ProductDeleteRequest;
import com.paystream.inventory.product.dto.request.ProductUpdateRequest;
import com.paystream.inventory.product.dto.response.ProductDetailResponse;
import com.paystream.inventory.product.dto.response.ProductPriceHistoryResponse;
import com.paystream.inventory.product.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductController {

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다.")
    BaseResponse<ProductDetailResponse> findById(@PathVariable Long productId);

    @Operation(summary = "상품 상세 및 재고 조회", description = "체크인/아웃 날짜를 포함하여 상품 상세 정보와 재고 상태를 조회합니다.")
    BaseResponse<ProductDetailResponse> findByIdAndStock(
            @PathVariable Long productId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate);

    @Operation(
            summary = "상품 생성",
            description = "멀티파트 폼 데이터를 이용해 신규 상품을 생성합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Long> create(
            HttpServletRequest request,
            @Valid @RequestPart("createRequest") ProductCreateRequest createRequest,
            @RequestPart("file") List<MultipartFile> files);

    @Operation(
            summary = "상품 수정",
            description = "기존 상품 정보를 업데이트합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Long> update(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Valid @RequestBody ProductUpdateRequest updateRequest);

    @Operation(
            summary = "상품 삭제",
            description = "상품을 삭제 처리합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<String> delete(
            @PathVariable Long productId,
            HttpServletRequest request,
            @Valid @RequestBody ProductDeleteRequest deleteRequest);

    @Operation(
            summary = "내 상품 목록 조회",
            description = "로그인한 사용자가 소유한 상품 목록을 조회합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<List<ProductResponse>> findOwnedProductList(HttpServletRequest request);

    @Operation(summary = "역대 최저가 숙소 조회", description = "해당 숙소의 역대 ")
    BaseResponse<List<ProductPriceHistoryResponse>> getProductPriceHistory(
            @PathVariable Long productId);
}
