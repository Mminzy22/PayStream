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
        List<StoreDto.Response> response = storeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    @Override
    public ResponseEntity<StoreDto.Response> getStoreById(@PathVariable String id) {
        return ResponseEntity.ok(storeService.findById(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<UUID> addStore(@RequestBody StoreDto.Request request) {
        UUID uuid = storeService.create(request);
        return ResponseEntity.ok(uuid);
    }

    @PutMapping("{id}")
    @Override
    public ResponseEntity<StoreDto.Response> updateStore(@PathVariable String id, @RequestBody StoreDto.Request request) {
        return null;
    }

    @DeleteMapping("{id}")
    @Override
    public ResponseEntity<StoreDto.Response> deleteStore(@PathVariable String id) {
        return null;
    }
}
