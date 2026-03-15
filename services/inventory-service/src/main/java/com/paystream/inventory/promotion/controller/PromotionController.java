package com.paystream.inventory.promotion.controller;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.promotion.dto.request.PromotionRequest;
import com.paystream.inventory.promotion.dto.response.PromotionResponse;
import com.paystream.inventory.promotion.service.PromotionFindService;
import com.paystream.inventory.promotion.service.PromotionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionFindService promotionFindService;

    @GetMapping
    public BaseResponse<List<PromotionResponse>> findAll() {
        List<PromotionResponse> response = promotionFindService.findAll();
        return BaseResponse.ok(response);
    }

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
    public BaseResponse<Long> create(@RequestBody PromotionRequest request) {
        Long id = promotionService.create(request);
        return BaseResponse.created(id);
    }

    @PutMapping("/{id}")
    public BaseResponse<Long> update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        Long updatedId = promotionService.update(id, request);
        return BaseResponse.ok(updatedId);
    }

    /**
     * 프로모션 삭제를 종료로 처리 (상태값 변경)
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        promotionService.finished(id);
        return BaseResponse.of(HttpStatus.OK, "성공적으로 처리가 완료되었습니다.", null);
    }
}
