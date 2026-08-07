package com.foodexpress.delivery.model.dto;

import com.foodexpress.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class DeliveryResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfo {
        private String id;
        private String orderId;
        private String customerId;
        private String customerName;
        private String customerPhone;
        private String restaurantId;
        private String restaurantName;
        private String deliveryPartnerId;
        private String deliveryPartnerName;
        private String deliveryPartnerPhone;
        private DeliveryStatus status;
        private String pickupAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;
        private String deliveryAddress;
        private Double deliveryLatitude;
        private Double deliveryLongitude;
        private String deliveryInstructions;
        private Integer estimatedPickupMinutes;
        private Integer estimatedDeliveryMinutes;
        private Instant assignedAt;
        private Instant pickedUpAt;
        private Instant deliveredAt;
        private Integer customerRating;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerInfo {
        private String id;
        private String name;
        private String phone;
        private String vehicleType;
        private String vehicleNumber;
        private String profilePicture;
        private Boolean isAvailable;
        private Boolean isOnline;
        private Boolean isVerified;
        private Double currentLatitude;
        private Double currentLongitude;
        private Integer totalDeliveries;
        private Double rating;
        private String currentOrderId;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerStats {
        private Integer totalDeliveries;
        private Integer todayDeliveries;
        private Double rating;
        private Integer totalRatings;
    }
}
