package com.foodexpress.delivery.controller;

import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.dto.PagedResponse;
import com.foodexpress.delivery.model.dto.DeliveryRequest;
import com.foodexpress.delivery.model.dto.DeliveryResponse;
import com.foodexpress.delivery.service.DeliveryPartnerService;
import com.foodexpress.delivery.service.DeliveryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Delivery management endpoints")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryPartnerService partnerService;

    // public/Customer endpoints

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery by order ID")
    public ResponseEntity<ApiResponse<DeliveryResponse.DeliveryInfo>> getDeliveryByOrderId(
            @PathVariable String orderId) {

        DeliveryResponse.DeliveryInfo delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(delivery));
    }

    // delivery Partner endpoints

    @PostMapping("/partner/register")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Register as delivery partner")
    public ResponseEntity<ApiResponse<DeliveryResponse.PartnerInfo>> registerPartner(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DeliveryRequest.RegisterPartner request) {

        DeliveryResponse.PartnerInfo partner = partnerService.registerPartner(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(partner, "Registration successful"));
    }

    @GetMapping("/partner/profile")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Get partner profile")
    public ResponseEntity<ApiResponse<DeliveryResponse.PartnerInfo>> getPartnerProfile(
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.PartnerInfo partner = partnerService.getPartnerProfile(partnerId);
        return ResponseEntity.ok(ApiResponse.success(partner));
    }

    @PostMapping("/partner/online")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Go online")
    public ResponseEntity<ApiResponse<DeliveryResponse.PartnerInfo>> goOnline(
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.PartnerInfo partner = partnerService.goOnline(partnerId);
        return ResponseEntity.ok(ApiResponse.success(partner, "You are now online"));
    }

    @PostMapping("/partner/offline")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Go offline")
    public ResponseEntity<ApiResponse<DeliveryResponse.PartnerInfo>> goOffline(
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.PartnerInfo partner = partnerService.goOffline(partnerId);
        return ResponseEntity.ok(ApiResponse.success(partner, "You are now offline"));
    }

    @PostMapping("/partner/location")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Update location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @RequestHeader("X-User-Id") String partnerId,
            @Valid @RequestBody DeliveryRequest.UpdateLocation request) {

        deliveryService.updateLocation(partnerId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/partner/active")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Get active deliveries")
    public ResponseEntity<ApiResponse<List<DeliveryResponse.DeliveryInfo>>> getActiveDeliveries(
            @RequestHeader("X-User-Id") String partnerId) {

        List<DeliveryResponse.DeliveryInfo> deliveries = deliveryService.getActiveDeliveriesForPartner(partnerId);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }

    @GetMapping("/partner/history")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Get delivery history")
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryResponse.DeliveryInfo>>> getDeliveryHistory(
            @RequestHeader("X-User-Id") String partnerId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<DeliveryResponse.DeliveryInfo> page = deliveryService.getDeliveryHistoryForPartner(partnerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    // delivery actions

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Accept delivery")
    public ResponseEntity<ApiResponse<DeliveryResponse.DeliveryInfo>> acceptDelivery(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.DeliveryInfo delivery = deliveryService.acceptDelivery(id, partnerId);
        return ResponseEntity.ok(ApiResponse.success(delivery, "Delivery accepted"));
    }

    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Pickup order")
    public ResponseEntity<ApiResponse<DeliveryResponse.DeliveryInfo>> pickupOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.DeliveryInfo delivery = deliveryService.pickupOrder(id, partnerId);
        return ResponseEntity.ok(ApiResponse.success(delivery, "Order picked up"));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Start delivery")
    public ResponseEntity<ApiResponse<DeliveryResponse.DeliveryInfo>> startDelivery(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.DeliveryInfo delivery = deliveryService.startDelivery(id, partnerId);
        return ResponseEntity.ok(ApiResponse.success(delivery, "Delivery started"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    @Operation(summary = "Complete delivery")
    public ResponseEntity<ApiResponse<DeliveryResponse.DeliveryInfo>> completeDelivery(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String partnerId) {

        DeliveryResponse.DeliveryInfo delivery = deliveryService.completeDelivery(id, partnerId);
        return ResponseEntity.ok(ApiResponse.success(delivery, "Delivery completed"));
    }

    // admin endpoints

    @PostMapping("/partner/{partnerId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify delivery partner (Admin)")
    public ResponseEntity<ApiResponse<DeliveryResponse.PartnerInfo>> verifyPartner(
            @PathVariable String partnerId) {

        DeliveryResponse.PartnerInfo partner = partnerService.verifyPartner(partnerId);
        return ResponseEntity.ok(ApiResponse.success(partner, "Partner verified"));
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
