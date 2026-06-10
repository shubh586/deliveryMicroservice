package com.foodexpress.order.service;

import com.foodexpress.common.enums.OrderStatus;
import com.foodexpress.common.enums.PaymentStatus;
import com.foodexpress.common.exception.BadRequestException;
import com.foodexpress.common.exception.ForbiddenException;
import com.foodexpress.common.exception.ResourceNotFoundException;

import com.foodexpress.order.model.dto.OrderRequest;
import com.foodexpress.order.model.dto.OrderResponse;
import com.foodexpress.order.model.entity.Order;
import com.foodexpress.order.model.entity.OrderItem;
import com.foodexpress.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;


    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("2.99");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax

    @Transactional
    public OrderResponse.OrderInfo createOrder(String customerId, OrderRequest.CreateOrder request,
                                               String customerEmail) {
        logger.info("Creating order for customer: {} at restaurant: {}", customerId, request.getRestaurantId());

        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryCity(request.getDeliveryCity())
                .deliveryPostalCode(request.getDeliveryPostalCode())
                .deliveryLatitude(request.getDeliveryLatitude())
                .deliveryLongitude(request.getDeliveryLongitude())
                .deliveryInstructions(request.getDeliveryInstructions())
                .customerPhone(request.getCustomerPhone())
                .customerName(request.getCustomerName())
                .paymentMethod(request.getPaymentMethod())
                .deliveryFee(DEFAULT_DELIVERY_FEE)
                .build();

        // Add items
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .menuItemId(itemRequest.getMenuItemId())
                    .name(itemRequest.getName())
                    .price(itemRequest.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .specialInstructions(itemRequest.getSpecialInstructions())
                    .build();
            order.addItem(item);
        }

        // Calculate totals
        order.calculateTotals();
        order.setTax(order.getSubtotal().multiply(TAX_RATE));
        order.setTotalAmount(order.getSubtotal().add(order.getDeliveryFee()).add(order.getTax()));

        order = orderRepository.save(order);
        logger.info("Order created: {}", order.getId());

        return toOrderInfo(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse.OrderInfo getOrderById(String orderId, String userId, String userRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Verify access
        if (!canAccessOrder(order, userId, userRole)) {
            throw new ForbiddenException("You don't have access to this order");
        }

        return toOrderInfo(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.OrderSummary> getCustomerOrders(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toOrderSummary);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.OrderSummary> getRestaurantOrders(String restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(this::toOrderSummary);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.OrderInfo> getActiveRestaurantOrders(String restaurantId, Pageable pageable) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP
        );

        return orderRepository.findByRestaurantIdAndStatusIn(restaurantId, activeStatuses, pageable)
                .map(this::toOrderInfo);
    }

    // Restaurant operations

    @Transactional
    public OrderResponse.OrderInfo confirmOrder(String orderId, String restaurantId) {
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateStatusTransition(order.getStatus(), OrderStatus.CONFIRMED);

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());
        order = orderRepository.save(order);

        logger.info("Order confirmed: {}", orderId);

        return toOrderInfo(order);
    }

    @Transactional
    public OrderResponse.OrderInfo startPreparing(String orderId, String restaurantId) {
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateStatusTransition(order.getStatus(), OrderStatus.PREPARING);

        order.setStatus(OrderStatus.PREPARING);
        order = orderRepository.save(order);

        logger.info("Order preparation started: {}", orderId);

        return toOrderInfo(order);
    }

    @Transactional
    public OrderResponse.OrderInfo markReady(String orderId, String restaurantId) {
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateStatusTransition(order.getStatus(), OrderStatus.READY_FOR_PICKUP);

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setPreparedAt(Instant.now());
        order = orderRepository.save(order);



        logger.info("Order ready for pickup: {}", orderId);

        return toOrderInfo(order);
    }

    // Customer operations

    @Transactional
    public OrderResponse.OrderInfo cancelOrder(String orderId, String customerId, String reason) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getStatus().canCancel()) {
            throw new BadRequestException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(reason);
        order = orderRepository.save(order);

        logger.info("Order cancelled: {} - Reason: {}", orderId, reason);

        return toOrderInfo(order);
    }



    @Transactional
    public OrderResponse.OrderInfo confirmPayment(String orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setPaymentId(paymentId);
        order = orderRepository.save(order);

        logger.info("Payment confirmed for order: {}", orderId);

        return toOrderInfo(order);
    }

    private boolean canAccessOrder(Order order, String userId, String userRole) {
        if ("ROLE_ADMIN".equals(userRole)) return true;
        if (order.getCustomerId().equals(userId)) return true;
        if ("ROLE_RESTAURANT_OWNER".equals(userRole)) return true; // Would need restaurant ownership check
        if ("ROLE_DELIVERY_PARTNER".equals(userRole) && userId.equals(order.getDeliveryPartnerId())) return true;
        return false;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        // Simple validation - in real app, would have a proper state machine
        List<OrderStatus> validTransitions = switch (current) {
            case PENDING -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED);
            case PREPARING -> List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED);
            case READY_FOR_PICKUP -> List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED);
            case OUT_FOR_DELIVERY -> List.of(OrderStatus.DELIVERED);
            default -> List.of();
        };

        if (!validTransitions.contains(target)) {
            throw new BadRequestException("Invalid status transition from " + current + " to " + target);
        }
    }

    private OrderResponse.OrderInfo toOrderInfo(Order order) {
        return OrderResponse.OrderInfo.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .restaurantId(order.getRestaurantId())
                .restaurantName(order.getRestaurantName())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemInfo.builder()
                                .id(item.getId())
                                .menuItemId(item.getMenuItemId())
                                .name(item.getName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .total(item.getTotal())
                                .specialInstructions(item.getSpecialInstructions())
                                .build())
                        .collect(Collectors.toList()))
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryCity(order.getDeliveryCity())
                .deliveryInstructions(order.getDeliveryInstructions())
                .deliveryPartnerId(order.getDeliveryPartnerId())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparedAt(order.getPreparedAt())
                .pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .build();
    }

    private OrderResponse.OrderSummary toOrderSummary(Order order) {
        return OrderResponse.OrderSummary.builder()
                .id(order.getId())
                .restaurantName(order.getRestaurantName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .itemCount(order.getItems().size())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
