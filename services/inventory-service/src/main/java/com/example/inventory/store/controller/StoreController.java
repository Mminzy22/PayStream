package com.example.inventory.store.controller;

import com.example.core.BaseResponse;
import com.example.inventory.store.dto.request.StoreCreateRequest;
import com.example.inventory.store.dto.request.StoreUpdateRequest;
import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.repository.StoreRepository;
import com.example.inventory.store.service.StoreCreateService;
import com.example.inventory.store.service.StoreFindService;
import com.example.inventory.store.service.StoreUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("stores")
public class StoreController {

    private final StoreFindService storeService;
    private final StoreCreateService storeCreateService;
    private final StoreUpdateService storeUpdateService;

    @GetMapping
    public BaseResponse<List<StoreResponse>> findAll(@Valid @ModelAttribute StoreUserFindRequest request,
                                                     @PageableDefault(page = 1, size = 10) Pageable pageable) {
        List<StoreResponse> responses = storeService.userFindStoreList(request, pageable);
        return BaseResponse.ok(responses);
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

}
