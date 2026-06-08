package com.foodexpress.restaurant.model.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class RestaurantRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRestaurant {
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be less than 100 characters")
        private String name;

        @Size(max = 1000, message = "Description must be less than 1000 characters")
        private String description;

        private String cuisineType;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "City is required")
        private String city;

        private String state;
        private String postalCode;
        private Double latitude;
        private Double longitude;
        private String phone;
        private String email;
        private String logoUrl;
        private String bannerUrl;
        private String openingHours;
        private Integer avgDeliveryTime;
        private Double minOrderAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRestaurant {
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String city;
        private String state;
        private String postalCode;
        private Double latitude;
        private Double longitude;
        private String phone;
        private String email;
        private String logoUrl;
        private String bannerUrl;
        private String openingHours;
        private Integer avgDeliveryTime;
        private Double minOrderAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateMenuItem {
        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private String categoryId;
        private String imageUrl;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isSpicy;
        private Integer preparationTime;
        private Integer calories;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMenuItem {
        private String name;
        private String description;
        private BigDecimal price;
        private String categoryId;
        private String imageUrl;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isSpicy;
        private Boolean isAvailable;
        private Integer preparationTime;
        private Integer calories;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCategory {
        @NotBlank(message = "Category name is required")
        private String name;

        private String description;
        private Integer displayOrder;
    }
}

