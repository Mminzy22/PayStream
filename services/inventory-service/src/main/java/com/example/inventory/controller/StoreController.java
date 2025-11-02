package com.example.inventory.controller;

import com.example.core.BaseResponse;
import com.example.inventory.dto.store.StoreResponse;
import com.example.inventory.dto.store.request.StoreUserFindRequest;
import com.example.inventory.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

}
