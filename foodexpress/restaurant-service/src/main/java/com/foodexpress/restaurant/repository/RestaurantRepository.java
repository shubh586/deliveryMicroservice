package com.foodexpress.restaurant.repository;

import com.foodexpress.restaurant.model.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, String> {
    Optional<Restaurant> findByIdAndOwnerId(String id, String ownerId);

    List<Restaurant> findByOwnerId(String ownerId);

    Page<Restaurant> findByIsVerifiedTrue(Pageable pageable);

    Page<Restaurant> findByIsVerifiedTrueAndIsOpenTrue(Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.isVerified = true " +
            "AND (:city IS NULL OR LOWER(r.city) = LOWER(:city)) " +
            "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisineType))")
    Page<Restaurant> findByCityAndCuisine(@Param("city") String city,
                                          @Param("cuisineType") String cuisineType,
                                          Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.isVerified = true " +
            "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Restaurant> searchRestaurants(@Param("query") String query, Pageable pageable);
}
