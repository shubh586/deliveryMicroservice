package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.model.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(String restaurantId);

    List<MenuItem> findByRestaurantId(String restaurantId);

    List<MenuItem> findByCategoryId(String categoryId);

    Optional<MenuItem> findByIdAndRestaurantId(String id, String restaurantId);

    void deleteByIdAndRestaurantId(String id, String restaurantId);
}
