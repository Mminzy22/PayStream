package com.example.inventory.controller;

import com.example.inventory.dto.request.StoreRequest;
import com.example.inventory.dto.response.StoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Store API" , description = "가게 관련 API 목록입니다.")
public interface StoreController {

    @Operation(description = "가게 목록")
    public ResponseEntity<List<StoreResponse>> getStores();

    @Operation(description = "가게 id 검색, 상세조회")
    public ResponseEntity<StoreResponse> getStoreById(String id);

    @Operation(description = "가게 등록")
    public ResponseEntity<UUID> addStore(StoreRequest request);

    @Operation(description = "가게 수정")
    public ResponseEntity<StoreResponse> updateStore(String id, StoreRequest request);

    @Operation(description = "가게 삭제")
    public ResponseEntity<StoreResponse> deleteStore(String id);

}
