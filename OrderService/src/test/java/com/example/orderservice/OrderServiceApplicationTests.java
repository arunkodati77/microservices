package com.example.orderservice;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.controller.OrderController;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.StockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Unit tests for controller
@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    void testCreateOrderSuccess() throws Exception {
        when(inventoryClient.getStock("prod1")).thenReturn(new StockResponse("prod1", 100));

        String orderJson = "{\"productId\":\"prod1\",\"quantity\":5}";
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Order created for product: prod1"));
    }

    @Test
    void testCreateOrderInsufficientStock() throws Exception {
        when(inventoryClient.getStock("prod1")).thenReturn(new StockResponse("prod1", 2));

        String orderJson = "{\"productId\":\"prod1\",\"quantity\":5}";
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient stock for product: prod1"));
    }

    @Test
    void testCreateOrderExactStock() throws Exception {
        when(inventoryClient.getStock("prod1")).thenReturn(new StockResponse("prod1", 5));

        String orderJson = "{\"productId\":\"prod1\",\"quantity\":5}";
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Order created for product: prod1"));
    }

    @Test
    void testCreateOrderNullStockResponse() throws Exception {
        when(inventoryClient.getStock("prod1")).thenReturn(null);

        String orderJson = "{\"productId\":\"prod1\",\"quantity\":5}";
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().is5xxServerError()); // Expecting NPE handling
    }
}

// Integration test
@SpringBootTest
@ContextConfiguration(classes = OrderServiceApplication.class)
class OrderServiceIntegrationTests {

    @Autowired
    private OrderController orderController;

    @MockBean
    private InventoryClient inventoryClient;

    @BeforeEach
    void setUp() {
        when(inventoryClient.getStock("prod1")).thenReturn(new StockResponse("prod1", 100));
        when(inventoryClient.getStock("prod2")).thenReturn(new StockResponse("prod2", 2));
        when(inventoryClient.getStock("prod3")).thenReturn(new StockResponse("prod3", 0));
    }

    @Test
    void testFullFlowCreateOrderSuccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId("prod1");
        request.setQuantity(5);

        var response = orderController.createOrder(request);
        assert response.getStatusCode().is2xxSuccessful();
        assert response.getBody().equals("Order created for product: prod1");
    }

    @Test
    void testFullFlowCreateOrderInsufficientStock() {
        OrderRequest request = new OrderRequest();
        request.setProductId("prod2");
        request.setQuantity(5);

        var response = orderController.createOrder(request);
        assert response.getStatusCode().is4xxClientError();
        assert response.getBody().equals("Insufficient stock for product: prod2");
    }

    @Test
    void testFullFlowCreateOrderZeroStock() {
        OrderRequest request = new OrderRequest();
        request.setProductId("prod3");
        request.setQuantity(1);

        var response = orderController.createOrder(request);
        assert response.getStatusCode().is4xxClientError();
        assert response.getBody().equals("Insufficient stock for product: prod3");
    }
}

// Test for application startup
@SpringBootTest
class OrderServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring context loads successfully
        assertNotNull(OrderServiceApplication.class);
    }

    @Test
    void mainMethodRuns() {
        // This doesn't truly "run" the app but ensures the main method is callable
        OrderServiceApplication.main(new String[]{});
    }
}