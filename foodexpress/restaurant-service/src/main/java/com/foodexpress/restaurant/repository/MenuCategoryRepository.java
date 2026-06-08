package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.model.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {

    List<MenuCategory> findByRestaurantIdOrderByDisplayOrderAsc(String restaurantId);

    void deleteByIdAndRestaurantId(String id, String restaurantId);
}
