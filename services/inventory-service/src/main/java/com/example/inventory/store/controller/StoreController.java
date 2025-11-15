package com.example.inventory.store.controller;

import com.example.core.BaseResponse;
import com.example.inventory.store.dto.request.StoreCreateRequest;
import com.example.inventory.store.dto.request.StoreDeleteRequest;
import com.example.inventory.store.dto.request.StoreUpdateRequest;
import com.example.inventory.store.dto.request.StoreListFindRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.service.StoreCreateService;
import com.example.inventory.store.service.StoreDeleteService;
import com.example.inventory.store.service.StoreFindService;
import com.example.inventory.store.service.StoreUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("stores")
public class StoreController {

    private final StoreFindService storeFindService;
    private final StoreCreateService storeCreateService;
    private final StoreUpdateService storeUpdateService;
    private final StoreDeleteService storeDeleteService;

    @GetMapping
    public BaseResponse<List<StoreResponse>> findAll(@Valid @ModelAttribute StoreListFindRequest request,
                                                     @PageableDefault(page = 1, size = 10) Pageable pageable) {
        List<StoreResponse> responses = storeFindService.userFindStoreList(request, pageable);
        return BaseResponse.ok(responses);
    }

    @GetMapping("{id}")
    public BaseResponse<StoreResponse> findById(@PathVariable Long id,
                                                LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        StoreResponse store = storeFindService.findStore(id, checkInDate, checkOutDate, personCount);

        return BaseResponse.ok(store);
    }

    @PostMapping
    public BaseResponse<Long> created(@RequestBody StoreCreateRequest request) {
        Long id = storeCreateService.create(request);
        return BaseResponse.created(id);
    }

    @PutMapping("{id}")
    public BaseResponse<StoreResponse> updated(@PathVariable Long id, @RequestBody StoreUpdateRequest request) {
        StoreResponse response = storeUpdateService.update(id, request);
        return BaseResponse.ok(response);
    }

    @DeleteMapping
    public BaseResponse<Void> deleted(@RequestBody StoreDeleteRequest request) {
        storeDeleteService.deleted(request);
        return BaseResponse.ok();
    }

}
