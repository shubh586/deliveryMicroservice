package com.foodexpress.restaurant.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MenuService {
    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;



    @Transactional
    public RestaurantResponse.CategoryWithItems createCategory(String restaurantId, String ownerId,
                                                               RestaurantRequest.CreateCategory request) {
        validateOwnership(restaurantId, ownerId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        MenuCategory category = MenuCategory.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        category = menuCategoryRepository.save(category);
        logger.info("Category created: {} for restaurant: {}", category.getId(), restaurantId);

        // List.of() or new ArrayList<>() instead of collections logic
        return RestaurantResponse.CategoryWithItems.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .items(new ArrayList<>())
                .build();
    }

    @Transactional
    public void deleteCategory(String restaurantId, String categoryId, String ownerId) {
        validateOwnership(restaurantId, ownerId);
        menuCategoryRepository.deleteByIdAndRestaurantId(categoryId, restaurantId);
        logger.info("Category deleted: {}", categoryId);
    }



    @Transactional
    public RestaurantResponse.MenuItemInfo createMenuItem(String restaurantId, String ownerId,
                                                          RestaurantRequest.CreateMenuItem request) {
        validateOwnership(restaurantId, ownerId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        MenuCategory category = null;
        if (request.getCategoryId() != null) {
            category = menuCategoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
                .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
                .isSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false)
                .preparationTime(request.getPreparationTime() != null ? request.getPreparationTime() : 15)
                .calories(request.getCalories())
                .build();

        item = menuItemRepository.save(item);
        logger.info("Menu item created: {} for restaurant: {}", item.getId(), restaurantId);
        return toMenuItemInfo(item);
    }

    @Transactional
    public RestaurantResponse.MenuItemInfo updateMenuItem(String restaurantId, String itemId,
                                                          String ownerId, RestaurantRequest.UpdateMenuItem request) {
        validateOwnership(restaurantId, ownerId);

        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsVegetarian() != null) item.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsVegan() != null) item.setIsVegan(request.getIsVegan());
        if (request.getIsSpicy() != null) item.setIsSpicy(request.getIsSpicy());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
        if (request.getPreparationTime() != null) item.setPreparationTime(request.getPreparationTime());
        if (request.getCalories() != null) item.setCalories(request.getCalories());

        if (request.getCategoryId() != null) {
            MenuCategory category = menuCategoryRepository.findById(request.getCategoryId()).orElse(null);
            item.setCategory(category);
        }

        item = menuItemRepository.save(item);
        logger.info("Menu item updated: {}", itemId);

        return toMenuItemInfo(item);
    }

    @Transactional
    public void deleteMenuItem(String restaurantId, String itemId, String ownerId) {
        validateOwnership(restaurantId, ownerId);
        menuItemRepository.deleteByIdAndRestaurantId(itemId, restaurantId);
        logger.info("Menu item deleted: {}", itemId);
    }

    @Transactional
    public void toggleItemAvailability(String restaurantId, String itemId, String ownerId) {
        validateOwnership(restaurantId, ownerId);

        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemId));

        item.setIsAvailable(!item.getIsAvailable());
        menuItemRepository.save(item);

        logger.info("Menu item {} availability toggled to {}", itemId, item.getIsAvailable());
    }


    @Transactional(readOnly = true)
    public List<RestaurantResponse.MenuItemInfo> getMenuItems(String restaurantId) {
        List<RestaurantResponse.MenuItemInfo> itemInfos = new ArrayList<>();

        for (MenuItem item : menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)) {
            itemInfos.add(toMenuItemInfo(item));
        }

        return itemInfos;
    }

    private void validateOwnership(String restaurantId, String ownerId) {
        if (!restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId).isPresent()) {
            throw new ForbiddenException("You don't own this restaurant");
        }
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
