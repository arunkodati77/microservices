package com.example.orderservice.controller;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.model.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final InventoryClient inventoryClient;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        var stock = inventoryClient.getStock(orderRequest.getProductId());
        if (stock == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve stock information for product: " + orderRequest.getProductId());
        }
        if (stock.getQuantity() >= orderRequest.getQuantity()) {
            return ResponseEntity.ok("Order created for product: " + orderRequest.getProductId());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Insufficient stock for product: " + orderRequest.getProductId());
        }
    }
}