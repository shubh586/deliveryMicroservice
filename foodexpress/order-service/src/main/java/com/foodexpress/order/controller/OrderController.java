package com.foodexpress.order.controller;

import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.dto.PagedResponse;
import com.foodexpress.order.model.dto.OrderRequest;
import com.foodexpress.order.model.dto.OrderResponse;
import com.foodexpress.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    // Customer endpoints

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> createOrder(
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader(value = "X-User-Email", required = false) String customerEmail,
            @Valid @RequestBody OrderRequest.CreateOrder request) {

        OrderResponse.OrderInfo order = orderService.createOrder(customerId, request, customerEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> getOrderById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        OrderResponse.OrderInfo order = orderService.getOrderById(id, userId, userRole);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse.OrderSummary>>> getMyOrders(
            @RequestHeader("X-User-Id") String customerId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<OrderResponse.OrderSummary> page = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> cancelOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody OrderRequest.CancelOrder request) {

        OrderResponse.OrderInfo order = orderService.cancelOrder(id, customerId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }

    // Restaurant endpoints

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get restaurant orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse.OrderSummary>>> getRestaurantOrders(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<OrderResponse.OrderSummary> page = orderService.getRestaurantOrders(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @GetMapping("/restaurant/{restaurantId}/active")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get active restaurant orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse.OrderInfo>>> getActiveRestaurantOrders(
            @PathVariable String restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<OrderResponse.OrderInfo> page = orderService.getActiveRestaurantOrders(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Confirm order (Restaurant)")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> confirmOrder(
            @PathVariable String id,
            @RequestParam String restaurantId) {

        OrderResponse.OrderInfo order = orderService.confirmOrder(id, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order confirmed"));
    }

    @PostMapping("/{id}/preparing")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Start preparing order (Restaurant)")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> startPreparing(
            @PathVariable String id,
            @RequestParam String restaurantId) {

        OrderResponse.OrderInfo order = orderService.startPreparing(id, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order preparation started"));
    }

    @PostMapping("/{id}/ready")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Mark order as ready (Restaurant)")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> markReady(
            @PathVariable String id,
            @RequestParam String restaurantId) {

        OrderResponse.OrderInfo order = orderService.markReady(id, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order is ready for pickup"));
    }

    // Payment webhook

    @PostMapping("/{id}/payment")
    @Operation(summary = "Confirm payment (Webhook)")
    public ResponseEntity<ApiResponse<OrderResponse.OrderInfo>> confirmPayment(
            @PathVariable String id,
            @RequestParam String paymentId) {

        OrderResponse.OrderInfo order = orderService.confirmPayment(id, paymentId);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment confirmed"));
    }

    private <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}

