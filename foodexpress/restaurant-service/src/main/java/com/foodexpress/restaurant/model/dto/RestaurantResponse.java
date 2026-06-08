package com.foodexpress.restaurant.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class RestaurantResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantInfo {
        private String id;
        private String ownerId;
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
        private Double rating;
        private Integer totalRatings;
        private Boolean isOpen;
        private Boolean isVerified;
        private String openingHours;
        private Integer avgDeliveryTime;
        private Double minOrderAmount;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantWithMenu {
        private RestaurantInfo restaurant;
        private List<CategoryWithItems> menu;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryWithItems {
        private String id;
        private String name;
        private String description;
        private Integer displayOrder;
        private List<MenuItemInfo> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemInfo {
        private String id;
        private String categoryId;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isSpicy;
        private Boolean isAvailable;
        private Integer preparationTime;
        private Integer calories;
    }
}
