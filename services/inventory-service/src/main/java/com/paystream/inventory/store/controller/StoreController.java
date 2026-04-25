package com.paystream.inventory.store.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.store.dto.request.*;
import com.paystream.inventory.store.dto.response.StoreBaseResponse;
import com.paystream.inventory.store.dto.response.StoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Tag(name = "Store", description = "가게 관련 API")
public interface StoreController {

    @Operation(summary = "가게 전체 조회")
    BaseResponse<PageResponse<StoreResponse>> findAll(
            StoreListFindRequest request, Pageable pageable);

    @Operation(summary = "가게 상세 조회")
    BaseResponse<StoreResponse> findById(Long id, StoreFindRequest request);

    @Operation(
            summary = "가게 생성",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Long> created(HttpServletRequest request, StoreCreateRequest createRequest);

    @Operation(
            summary = "가게 정보 수정",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<StoreResponse> updated(
            Long id, HttpServletRequest request, StoreUpdateRequest updateRequest);

    @Operation(
            summary = "가게 삭제",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<String> deleted(HttpServletRequest request, StoreDeleteRequest deleteRequest);

    @Operation(
            summary = "내 소유 가게 페이징 조회",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<PageResponse<StoreResponse>> findOwnedStores(
            StoreListFindRequest request, Pageable pageable, HttpServletRequest req);

    @Operation(
            summary = "내 소유 가게 리스트 조회",
            description = "소유자가 갖고 있는 가게에 대한 정보만을 조회한다. (상품, 재고 x)",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<List<StoreBaseResponse>> findOwnedStoreList(HttpServletRequest req);
}
