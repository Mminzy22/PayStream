package com.example.inventory.controller;

import com.example.inventory.dto.StoreDto;
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
    public ResponseEntity<List<StoreDto.Response>> getStores() {
        log.info("getStores trace");
        List<StoreDto.Response> response = storeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    @Override
    public ResponseEntity<StoreDto.Response> getStoreById(@PathVariable Integer id) {
        return null;
    }

    @PostMapping
    @Override
    public ResponseEntity<UUID> addStore(@RequestBody StoreDto.Request request) {
        log.info("addStore request: {}", request);
        UUID uuid = storeService.create(request);
        return ResponseEntity.ok(uuid);
    }

    @PutMapping
    @Override
    public ResponseEntity<StoreDto.Response> updateStore(@RequestBody StoreDto.Request request) {
        return null;
    }

    @DeleteMapping("{id}")
    @Override
    public ResponseEntity<StoreDto.Response> deleteStore(@PathVariable Integer id) {
        return null;
    }
}
