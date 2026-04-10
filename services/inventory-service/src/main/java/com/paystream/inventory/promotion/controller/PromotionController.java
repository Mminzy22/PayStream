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
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionFindService promotionFindService;

    /**
     * 할인 프로모션 리스트를 조회할 수 있다.
     *
     * @return 프로모션 List
     */
    @GetMapping
    public BaseResponse<Page<PromotionResponse>> findAll(
            @RequestBody PromotionFindRequest request,
            @PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<PromotionResponse> response = promotionFindService.findAll(request, pageable);
        return BaseResponse.ok(response);
    }

    /**
     * 할인 프로모션의 상세 내용을 조회할 수 있다.
     *
     * @param id 상품id
     * @return 프로모션
     */
    @GetMapping("/{id}")
    public BaseResponse<PromotionResponse> findById(@PathVariable Long id) {
        PromotionResponse response = promotionFindService.findById(id);
        return BaseResponse.ok(response);
    }

    /**
     * 할인 프로모션을 생성할 수 있다. 선택항목은 가게 또는 숙소(상품) 이며, 각각에 할인율을 적용할 수 있다.
     *
     * @param request 상품 생성을 위한 요청 값
     * @return 프로모션 id
     */
    @PostMapping
    public BaseResponse<Long> create(@Valid @RequestBody PromotionRequest request) {
        Long id = promotionService.create(request);
        return BaseResponse.created(id);
    }

    /**
     * 할인 프로모션을 수정할 수 있다.
     *
     * @param id 상품id
     * @param request 상품 수정을 위한 요청 값
     * @return 프로모션 i
     */
    @PutMapping("/{id}")
    public BaseResponse<Long> update(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequest request,
            HttpServletRequest httpServletRequest) {
        String currentUserId = getCurrentUserId(httpServletRequest);
        Long updatedId = promotionService.update(id, currentUserId, request);
        return BaseResponse.ok(updatedId);
    }

    /**
     * 프로모션 삭제를 종료로 처리 (상태값 변경)
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String currentUserId = getCurrentUserId(request);
        promotionService.finished(id, currentUserId);
        return BaseResponse.of(HttpStatus.OK, "성공적으로 처리가 완료되었습니다.", null);
    }
}
