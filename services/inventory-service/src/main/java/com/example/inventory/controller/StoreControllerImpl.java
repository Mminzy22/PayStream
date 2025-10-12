package com.example.inventory.controller;

import com.example.inventory.dto.request.StoreRequest;
import com.example.inventory.dto.response.StoreResponse;
import com.example.inventory.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreControllerImpl implements StoreController {

    private final StoreService storeService;

    @GetMapping
    @Override
    public ResponseEntity<List<StoreResponse>> getStores() {
        List<StoreResponse> response = storeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    @Override
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable String id) {
        return ResponseEntity.ok(storeService.findById(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<UUID> addStore(@RequestBody StoreRequest request) {
        UUID uuid = storeService.create(request);
        return ResponseEntity.ok(uuid);
    }

    @PutMapping("{id}")
    @Override
    public ResponseEntity<StoreResponse> updateStore(@PathVariable String id, @RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.update(id, request));
    }

    @DeleteMapping("{id}")
    @Override
    public ResponseEntity<StoreResponse> deleteStore(@PathVariable String id) {
        storeService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
