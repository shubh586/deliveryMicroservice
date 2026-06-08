package com.foodexpress.restaurant.controller;
import com.foodexpress.common.dto.ApiResponse;
import com.foodexpress.common.dto.PagedResponse;
import com.foodexpress.restaurant.model.dto.RestaurantRequest;
import com.foodexpress.restaurant.model.dto.RestaurantResponse;
import com.foodexpress.restaurant.service.MenuService;
import com.foodexpress.restaurant.service.RestaurantService;
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
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management endpoints")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    // Public endpoints

    @GetMapping
    @Operation(summary = "Get all restaurants")
    public ResponseEntity<ApiResponse<PagedResponse<RestaurantResponse.RestaurantInfo>>> getAllRestaurants(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RestaurantResponse.RestaurantInfo> page = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants")
    public ResponseEntity<ApiResponse<PagedResponse<RestaurantResponse.RestaurantInfo>>> searchRestaurants(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RestaurantResponse.RestaurantInfo> page = restaurantService.searchRestaurants(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter restaurants by city and cuisine")
    public ResponseEntity<ApiResponse<PagedResponse<RestaurantResponse.RestaurantInfo>>> filterRestaurants(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisineType,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RestaurantResponse.RestaurantInfo> page = restaurantService.filterRestaurants(city, cuisineType, pageable);
        return ResponseEntity.ok(ApiResponse.success(toPagedResponse(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID")
    public ResponseEntity<ApiResponse<RestaurantResponse.RestaurantInfo>> getRestaurantById(@PathVariable String id) {
        RestaurantResponse.RestaurantInfo restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    @GetMapping("/{id}/menu")
    @Operation(summary = "Get restaurant with full menu")
    public ResponseEntity<ApiResponse<RestaurantResponse.RestaurantWithMenu>> getRestaurantWithMenu(@PathVariable String id) {
        RestaurantResponse.RestaurantWithMenu restaurant = restaurantService.getRestaurantWithMenuById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }

    // Restaurant Owner endpoints

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new restaurant (Restaurant Owner only)")
    public ResponseEntity<ApiResponse<RestaurantResponse.RestaurantInfo>> createRestaurant(
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody RestaurantRequest.CreateRestaurant request) {

        RestaurantResponse.RestaurantInfo restaurant = restaurantService.createRestaurant(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(restaurant, "Restaurant created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Update restaurant (Owner only)")
    public ResponseEntity<ApiResponse<RestaurantResponse.RestaurantInfo>> updateRestaurant(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody RestaurantRequest.UpdateRestaurant request) {

        RestaurantResponse.RestaurantInfo restaurant = restaurantService.updateRestaurant(id, ownerId, request);
        return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant updated successfully"));
    }

    @PostMapping("/{id}/toggle-open")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Toggle restaurant open/closed status")
    public ResponseEntity<ApiResponse<Void>> toggleRestaurantOpen(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String ownerId) {

        restaurantService.toggleRestaurantOpen(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Restaurant status toggled"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get my restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse.RestaurantInfo>>> getMyRestaurants(
            @RequestHeader("X-User-Id") String ownerId) {

        List<RestaurantResponse.RestaurantInfo> restaurants = restaurantService.getMyRestaurants(ownerId);
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    // Menu management

    @PostMapping("/{id}/categories")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Create menu category")
    public ResponseEntity<ApiResponse<RestaurantResponse.CategoryWithItems>> createCategory(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody RestaurantRequest.CreateCategory request) {

        RestaurantResponse.CategoryWithItems category = menuService.createCategory(id, ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category created successfully"));
    }

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Add menu item")
    public ResponseEntity<ApiResponse<RestaurantResponse.MenuItemInfo>> createMenuItem(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody RestaurantRequest.CreateMenuItem request) {

        RestaurantResponse.MenuItemInfo item = menuService.createMenuItem(id, ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(item, "Menu item created successfully"));
    }

    @PutMapping("/{id}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Update menu item")
    public ResponseEntity<ApiResponse<RestaurantResponse.MenuItemInfo>> updateMenuItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody RestaurantRequest.UpdateMenuItem request) {

        RestaurantResponse.MenuItemInfo item = menuService.updateMenuItem(id, itemId, ownerId, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Menu item updated successfully"));
    }

    @DeleteMapping("/{id}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestHeader("X-User-Id") String ownerId) {

        menuService.deleteMenuItem(id, itemId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted successfully"));
    }

    // Admin endpoints

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify restaurant (Admin only)")
    public ResponseEntity<ApiResponse<Void>> verifyRestaurant(@PathVariable String id) {
        restaurantService.verifyRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Restaurant verified successfully"));
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

