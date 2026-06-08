package com.foodexpress.restaurant.service;

import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.exception.ResourceNotFoundException;
import com.foodexpress.restaurant.model.dto.RestaurantResponse;
import com.foodexpress.restaurant.model.entity.MenuCategory;
import com.foodexpress.restaurant.model.entity.MenuItem;
import com.foodexpress.restaurant.model.entity.Restaurant;
import com.foodexpress.restaurant.repository.MenuCategoryRepository;
import com.foodexpress.restaurant.repository.MenuItemRepository;
import com.foodexpress.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor


public class RestaurantService {
    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public Page<RestaurantResponse.RestaurantInfo> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findByIsVerifiedTrueAndIsOpenTrue(pageable)
                .map(this::toRestaurantInfo);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse.RestaurantInfo> searchRestaurants(String query, Pageable pageable) {
        return restaurantRepository.searchRestaurants(query, pageable)
                .map(this::toRestaurantInfo);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse.RestaurantInfo> filterRestaurants(String city, String cuisine, Pageable pageable) {
        return restaurantRepository.findByCityAndCuisine(city,cuisine,pageable).map(this::toRestaurantInfo);
    }
    @Transactional(readOnly = true)
    public RestaurantResponse.RestaurantInfo getRestaurantById(String id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return toRestaurantInfo(restaurant);
    }

    @Transactional(readOnly = true)
    public ApiResponse<RestaurantResponse.RestaurantWithMenu> getRestaurantWithMenuById(String id) {
        Restaurant restaurant=restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        RestaurantResponse.RestaurantInfo restaurantInfo=toRestaurantInfo(restaurant);

        List<MenuItem> list=menuItemRepository.findByRestaurantId(restaurant.getId());
        List<MenuCategory> menuCategories=menuCategoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(id);



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

    private RestaurantResponse.RestaurantInfo toRestaurantInfo(Restaurant restaurant) {
        return RestaurantResponse.RestaurantInfo.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwnerId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisineType(restaurant.getCuisineType())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .postalCode(restaurant.getPostalCode())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .logoUrl(restaurant.getLogoUrl())
                .bannerUrl(restaurant.getBannerUrl())
                .rating(restaurant.getRating())
                .totalRatings(restaurant.getTotalRatings())
                .isOpen(restaurant.getIsOpen())
                .isVerified(restaurant.getIsVerified())
                .openingHours(restaurant.getOpeningHours())
                .avgDeliveryTime(restaurant.getAvgDeliveryTime())
                .minOrderAmount(restaurant.getMinOrderAmount())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

}
