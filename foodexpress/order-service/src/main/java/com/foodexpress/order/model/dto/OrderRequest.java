package com.foodexpress.order.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrder {
        @NotBlank(message = "Restaurant ID is required")
        private String restaurantId;

        private String restaurantName;

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        private List<OrderItemRequest> items;

        @NotBlank(message = "Delivery address is required")
        private String deliveryAddress;

        private String deliveryCity;
        private String deliveryPostalCode;
        private Double deliveryLatitude;
        private Double deliveryLongitude;
        private String deliveryInstructions;

        private String customerPhone;
        private String customerName;

        private String paymentMethod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotBlank(message = "Menu item ID is required")
        private String menuItemId;

        @NotBlank(message = "Item name is required")
        private String name;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private String specialInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelOrder {
        @NotBlank(message = "Cancellation reason is required")
        private String reason;
    }
}
