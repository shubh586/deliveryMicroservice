package com.foodexpress.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when restaurant or menu changes
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RestaurantEvent extends BaseEvent {

    public static final String TOPIC = "restaurant-events";

    public static final String RESTAURANT_CREATED = "RESTAURANT_CREATED";
    public static final String RESTAURANT_UPDATED = "RESTAURANT_UPDATED";
    public static final String RESTAURANT_OPENED = "RESTAURANT_OPENED";
    public static final String RESTAURANT_CLOSED = "RESTAURANT_CLOSED";
    public static final String MENU_UPDATED = "MENU_UPDATED";
    public static final String MENU_ITEM_ADDED = "MENU_ITEM_ADDED";
    public static final String MENU_ITEM_REMOVED = "MENU_ITEM_REMOVED";

    private String restaurantId;
    private String ownerId;
    private String restaurantName;
    private String cuisineType;
    private Boolean isOpen;
    private RestaurantData restaurantData;
    private List<MenuItemData> menuItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantData {
        private String description;
        private String address;
        private String city;
        private String phone;
        private Double latitude;
        private Double longitude;
        private Double rating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemData {
        private String menuItemId;
        private String name;
        private String category;
        private BigDecimal price;
        private Boolean isAvailable;
        private Boolean isVegetarian;
    }
}

