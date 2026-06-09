package com.foodexpress.order.model.dto;

import com.foodexpress.common.enums.OrderStatus;
import com.foodexpress.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


public class OrderResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private String id;
        private String customerId;
        private String customerName;
        private String customerPhone;
        private String restaurantId;
        private String restaurantName;
        private OrderStatus status;
        private PaymentStatus paymentStatus;
        private List<OrderItemInfo> items;
        private BigDecimal subtotal;
        private BigDecimal deliveryFee;
        private BigDecimal tax;
        private BigDecimal discount;
        private BigDecimal totalAmount;
        private String deliveryAddress;
        private String deliveryCity;
        private String deliveryInstructions;
        private String deliveryPartnerId;
        private String paymentMethod;
        private Instant createdAt;
        private Instant confirmedAt;
        private Instant preparedAt;
        private Instant pickedUpAt;
        private Instant deliveredAt;
        private Instant cancelledAt;
        private String cancellationReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String id;
        private String menuItemId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal total;
        private String specialInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private String id;
        private String restaurantName;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private Integer itemCount;
        private Instant createdAt;
    }
}

