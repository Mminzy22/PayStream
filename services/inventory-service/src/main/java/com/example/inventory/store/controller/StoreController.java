package com.example.inventory.store.controller;

import com.example.core.BaseResponse;
import com.example.inventory.store.dto.request.StoreCreateRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.store.service.StoreService;
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

    private final StoreService storeService;

    @GetMapping
    public BaseResponse<List<StoreResponse>> findAll(@Valid @ModelAttribute StoreUserFindRequest request,
                                                     @PageableDefault(page = 1, size = 10) Pageable pageable) {
        List<StoreResponse> responses = storeService.userFindStoreList(request, pageable);
        return BaseResponse.ok(responses);
    }

    @PostMapping
    public BaseResponse<String> created(@RequestBody StoreCreateRequest request) {
        return null;
    }

}
