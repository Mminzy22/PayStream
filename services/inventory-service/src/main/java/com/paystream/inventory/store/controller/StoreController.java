package com.paystream.inventory.store.controller;

import static com.paystream.inventory.utils.AuthUtils.getCurrentUserId;

import com.paystream.core.BaseResponse;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.store.dto.request.*;
import com.paystream.inventory.store.dto.response.StoreBaseResponse;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.service.StoreCreateService;
import com.paystream.inventory.store.service.StoreDeleteService;
import com.paystream.inventory.store.service.StoreFindService;
import com.paystream.inventory.store.service.StoreUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreFindService storeFindService;
    private final StoreCreateService storeCreateService;
    private final StoreUpdateService storeUpdateService;
    private final StoreDeleteService storeDeleteService;

    @GetMapping
    public BaseResponse<PageResponse<StoreResponse>> findAll(
            @Valid @ModelAttribute StoreListFindRequest request,
            @PageableDefault(page = 1, size = 10) Pageable pageable) {
        PageResponse<StoreResponse> responses =
                storeFindService.userFindStoreList(request, pageable);
        return BaseResponse.ok(responses);
    }

    @GetMapping("{id}")
    public BaseResponse<StoreResponse> findById(
            @PathVariable Long id, @Valid @ModelAttribute StoreFindRequest request) {
        StoreResponse store = storeFindService.findStore(id, request);

        return BaseResponse.ok(store);
    }

    @PostMapping
    public BaseResponse<Long> created(
            HttpServletRequest request, @Valid @RequestBody StoreCreateRequest createRequest) {
        String hostId = getCurrentUserId(request);
        Long id = storeCreateService.create(hostId, createRequest);
        return BaseResponse.created(id);
    }

    @PutMapping("{id}")
    public BaseResponse<StoreResponse> updated(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody StoreUpdateRequest updateRequest) {
        String hostId = getCurrentUserId(request);
        StoreResponse response = storeUpdateService.update(id, hostId, updateRequest);
        return BaseResponse.ok(response);
    }

    @DeleteMapping
    public BaseResponse<String> deleted(
            HttpServletRequest request, @Valid @RequestBody StoreDeleteRequest deleteRequest) {
        String hostId = getCurrentUserId(request);
        storeDeleteService.deleted(hostId, deleteRequest);
        return BaseResponse.ok("성공적으로 제거 되었습니다.");
    }

    @GetMapping("/user")
    public BaseResponse<PageResponse<StoreResponse>> findOwnedStores(
            @Valid @ModelAttribute StoreListFindRequest request,
            @PageableDefault(page = 1, size = 10) Pageable pageable,
            HttpServletRequest req) {
        String hostId = getCurrentUserId(req);
        PageResponse<StoreResponse> response =
                storeFindService.listUserStores(hostId, request, pageable);

        return BaseResponse.ok(response);
    }

    /**
     * 소유자가 갖고 있는 가게에 대한 정보만을 조회한다. (상품, 재고 x)
     *
     * @param req
     * @return 기본적인 가게에 대한 정보
     */
    @GetMapping("/user/list")
    public BaseResponse<List<StoreBaseResponse>> findOwnedStoreList(HttpServletRequest req) {
        String hostId = getCurrentUserId(req);
        List<StoreBaseResponse> response = storeFindService.getUserStoreList(hostId);

        return BaseResponse.ok(response);
    }
}
