package com.foodexpress.delivery.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DeliveryRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterPartner {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Phone is required")
        private String phone;

        private String email;

        @NotBlank(message = "Vehicle type is required")
        private String vehicleType;

        @NotBlank(message = "Vehicle number is required")
        private String vehicleNumber;

        private String profilePicture;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateLocation {
        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateDelivery {
        @NotNull(message = "Rating is required")
        private Integer rating;

        private String feedback;
    }
}
