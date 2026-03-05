package com.foodexpress.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when order state changes
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends BaseEvent {

    public static final String TOPIC = "order-events";

    // Event types
    public static final String ORDER_PLACED = "ORDER_PLACED";
    public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
    public static final String ORDER_PREPARING = "ORDER_PREPARING";
    public static final String ORDER_READY = "ORDER_READY";
    public static final String ORDER_PICKED_UP = "ORDER_PICKED_UP";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String PAYMENT_RECEIVED = "PAYMENT_RECEIVED";

    private String orderId;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String restaurantId;
    private String restaurantName;
    private String deliveryPartnerId;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String cancellationReason;
    private List<OrderItemInfo> items;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String menuItemId;
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }
}

