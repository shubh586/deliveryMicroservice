package com.foodexpress.restaurant.service;

import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.exception.ForbiddenException;
import com.foodexpress.common.exception.ResourceNotFoundException;
import com.foodexpress.restaurant.model.dto.RestaurantRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public RestaurantResponse.RestaurantWithMenu getRestaurantWithMenuById(String id) {
        Restaurant restaurant=restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        RestaurantResponse.RestaurantInfo restaurantInfo=toRestaurantInfo(restaurant);

        List<MenuItem> list=menuItemRepository.findByRestaurantIdAndIsAvailableTrue(id);
        List<MenuCategory> menuCategories=menuCategoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(id);

        Map<String, List<MenuItem>> itemsByCategory = new HashMap<>();

        for (MenuItem item : list) {
            if (item.getCategory() != null) {
                String categoryId = item.getCategory().getId();
                itemsByCategory.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(item);
            }
        }

        List<RestaurantResponse.CategoryWithItems> menu = new ArrayList<>();

        for (MenuCategory cat : menuCategories) {
            List<RestaurantResponse.MenuItemInfo> itemInfos = new ArrayList<>();
            List<MenuItem> categoryItems = itemsByCategory.getOrDefault(cat.getId(), new ArrayList<>());

            for (MenuItem item : categoryItems) {
                itemInfos.add(toMenuItemInfo(item));
            }

            RestaurantResponse.CategoryWithItems categoryWithItems = RestaurantResponse.CategoryWithItems.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .description(cat.getDescription())
                    .displayOrder(cat.getDisplayOrder())
                    .items(itemInfos)
                    .build();
            menu.add(categoryWithItems);
        }


        return RestaurantResponse.RestaurantWithMenu.builder()
                .restaurant(toRestaurantInfo(restaurant))
                .menu(menu)
                .build();
    }


    @Transactional
    public RestaurantResponse.RestaurantInfo createRestaurant(String ownerId, RestaurantRequest.CreateRestaurant request) {
        logger.info("Creating restaurant for owner: {}", ownerId);

        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .cuisineType(request.getCuisineType())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .email(request.getEmail())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .openingHours(request.getOpeningHours())
                .avgDeliveryTime(request.getAvgDeliveryTime() != null ? request.getAvgDeliveryTime() : 30)
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0.0)
                .build();

        restaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant created: {}", restaurant.getId());
        return toRestaurantInfo(restaurant);
    }

    @Transactional
    public RestaurantResponse.RestaurantInfo updateRestaurant(String id, String ownerId, RestaurantRequest.UpdateRestaurant request) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ForbiddenException("btw u are unknown to me so good by"));

        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getCuisineType() != null) restaurant.setCuisineType(request.getCuisineType());
        if (request.getAddress() != null) restaurant.setAddress(request.getAddress());
        if (request.getCity() != null) restaurant.setCity(request.getCity());
        if (request.getState() != null) restaurant.setState(request.getState());
        if (request.getPostalCode() != null) restaurant.setPostalCode(request.getPostalCode());
        if (request.getLatitude() != null) restaurant.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) restaurant.setLongitude(request.getLongitude());
        if (request.getPhone() != null) restaurant.setPhone(request.getPhone());
        if (request.getEmail() != null) restaurant.setEmail(request.getEmail());
        if (request.getLogoUrl() != null) restaurant.setLogoUrl(request.getLogoUrl());
        if (request.getBannerUrl() != null) restaurant.setBannerUrl(request.getBannerUrl());
        if (request.getOpeningHours() != null) restaurant.setOpeningHours(request.getOpeningHours());
        if (request.getAvgDeliveryTime() != null) restaurant.setAvgDeliveryTime(request.getAvgDeliveryTime());
        if (request.getMinOrderAmount() != null) restaurant.setMinOrderAmount(request.getMinOrderAmount());

        restaurant = restaurantRepository.save(restaurant);
        logger.info("updated {}", restaurant.getId());
        return toRestaurantInfo(restaurant);
    }

    @Transactional
    public void toggleRestaurantOpen(String id, String ownerId) {
        Restaurant restaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ForbiddenException("btw u are unknown to me so good by"));

        restaurant.setIsOpen(!restaurant.getIsOpen());
        restaurantRepository.save(restaurant);
        logger.info("Restaurant {} is now {}", id, restaurant.getIsOpen() ? "OPEN" : "CLOSED");
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse.RestaurantInfo> getMyRestaurants(String ownerId) {
        List<RestaurantResponse.RestaurantInfo> restaurantInfos = new ArrayList<>();

        for (Restaurant restaurant : restaurantRepository.findByOwnerId(ownerId)) {
            restaurantInfos.add(toRestaurantInfo(restaurant));
        }
        return restaurantInfos;
    }



    @Transactional
    public void verifyRestaurant(String id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        restaurant.setIsVerified(true);
        restaurantRepository.save(restaurant);
        logger.info("Restaurant verified: {}", id);
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

    private RestaurantResponse.MenuItemInfo toMenuItemInfo(MenuItem item) {
        return RestaurantResponse.MenuItemInfo.builder()
                .id(item.getId())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .isVegetarian(item.getIsVegetarian())
                .isVegan(item.getIsVegan())
                .isSpicy(item.getIsSpicy())
                .isAvailable(item.getIsAvailable())
                .preparationTime(item.getPreparationTime())
                .calories(item.getCalories())
                .build();
    }

}
