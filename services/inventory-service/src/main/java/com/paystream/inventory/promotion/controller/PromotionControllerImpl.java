package com.paystream.inventory.promotion.controller;

import static com.paystream.inventory.utils.AuthUtils.getCurrentUserId;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.promotion.dto.request.PromotionFindRequest;
import com.paystream.inventory.promotion.dto.request.PromotionRequest;
import com.paystream.inventory.promotion.dto.response.PromotionResponse;
import com.paystream.inventory.promotion.service.PromotionFindService;
import com.paystream.inventory.promotion.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionControllerImpl implements PromotionController {

    private final PromotionService promotionService;
    private final PromotionFindService promotionFindService;

    @GetMapping
    @Override
    public BaseResponse<Page<PromotionResponse>> findAll(
            @RequestBody PromotionFindRequest request,
            @PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<PromotionResponse> response = promotionFindService.findAll(request, pageable);
        return BaseResponse.ok(response);
    }

    @GetMapping("/{id}")
    @Override
    public BaseResponse<PromotionResponse> findById(@PathVariable Long id) {
        PromotionResponse response = promotionFindService.findById(id);
        return BaseResponse.ok(response);
    }

    @PostMapping
    @Override
    public BaseResponse<Long> create(
            @Valid @RequestBody PromotionRequest request, HttpServletRequest httpServletRequest) {
        String currentUserId = getCurrentUserId(httpServletRequest);
        Long id = promotionService.create(currentUserId, request);
        return BaseResponse.created(id);
    }

    @PutMapping("/{id}")
    @Override
    public BaseResponse<Long> update(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request,
            HttpServletRequest httpServletRequest) {
        String currentUserId = getCurrentUserId(httpServletRequest);
        Long updatedId = promotionService.update(id, currentUserId, request);
        return BaseResponse.ok(updatedId);
    }

    @DeleteMapping("/{id}")
    @Override
    public BaseResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String currentUserId = getCurrentUserId(request);
        promotionService.finished(id, currentUserId);
        return BaseResponse.of(HttpStatus.OK, "성공적으로 처리가 완료되었습니다.", null);
    }
}
