package com.example.inventoryservice;

import com.example.inventoryservice.controller.InventoryController;
import com.example.inventoryservice.model.StockResponse;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Unit tests for controller
@WebMvcTest(InventoryController.class)
class InventoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void testGetStockSuccess() throws Exception {
        when(inventoryService.getStock("prod1")).thenReturn(new StockResponse("prod1", 100));

        mockMvc.perform(get("/api/inventory/prod1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is("prod1")))
                .andExpect(jsonPath("$.quantity", is(100)));
    }

    @Test
    void testGetStockZeroQuantity() throws Exception {
        when(inventoryService.getStock("prod3")).thenReturn(new StockResponse("prod3", 0));

        mockMvc.perform(get("/api/inventory/prod3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is("prod3")))
                .andExpect(jsonPath("$.quantity", is(0)));
    }
}

// Unit tests for service
class InventoryServiceTests {

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    void testGetStockExistingProduct() {
        StockResponse response = inventoryService.getStock("prod1");
        assert response.getProductId().equals("prod1");
        assert response.getQuantity() == 100;
    }

    @Test
    void testGetStockNonExistingProduct() {
        StockResponse response = inventoryService.getStock("prod999");
        assert response.getProductId().equals("prod999");
        assert response.getQuantity() == 0;
    }
}

// Integration test for the full application
@SpringBootTest
class InventoryServiceIntegrationTests {

    @Autowired
    private InventoryController inventoryController;

    @Test
    void testFullFlowGetStock() {
        StockResponse response = inventoryController.getStock("prod1");
        assert response.getProductId().equals("prod1");
        assert response.getQuantity() == 100;
    }

    @Test
    void testFullFlowGetStockNonExisting() {
        StockResponse response = inventoryController.getStock("prod999");
        assert response.getProductId().equals("prod999");
        assert response.getQuantity() == 0;
    }
}