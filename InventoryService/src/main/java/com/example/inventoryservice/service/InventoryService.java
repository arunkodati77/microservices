package com.example.inventoryservice.service;

import com.example.inventoryservice.model.StockResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();

    public InventoryService() {
        // Dummy data
        stock.put("prod1", 100);
        stock.put("prod2", 50);
    }

    public StockResponse getStock(String productId) {
        Integer quantity = stock.getOrDefault(productId, 0);
        return new StockResponse(productId, quantity);
    }
}