package com.paystream.inventory.promotion.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.promotion.dto.request.PromotionFindRequest;
import com.paystream.inventory.promotion.dto.request.PromotionRequest;
import com.paystream.inventory.promotion.dto.response.PromotionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Promotion", description = "할인 프로모션 관리 API")
public interface PromotionController {

    @Operation(summary = "프로모션 목록 조회", description = "조건에 맞는 프로모션 리스트를 페이징하여 조회합니다.")
    BaseResponse<Page<PromotionResponse>> findAll(PromotionFindRequest request, Pageable pageable);

    @Operation(summary = "프로모션 상세 조회", description = "ID를 통해 특정 프로모션의 상세 내용을 조회합니다.")
    BaseResponse<PromotionResponse> findById(Long id);

    @Operation(
            summary = "프로모션 생성",
            description = "가게 또는 상품에 적용될 새로운 할인 프로모션을 생성합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Long> create(PromotionRequest request, HttpServletRequest httpServletRequest);

    @Operation(
            summary = "프로모션 수정",
            description = "기존 프로모션의 정보를 수정합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Long> update(
            Long id, PromotionRequest request, HttpServletRequest httpServletRequest);

    @Operation(
            summary = "프로모션 종료(삭제)",
            description = "프로모션 상태를 종료로 변경하여 삭제 처리합니다.",
            security = {@SecurityRequirement(name = "X-Auth-User-Id")})
    BaseResponse<Void> delete(Long id, HttpServletRequest request);
}
