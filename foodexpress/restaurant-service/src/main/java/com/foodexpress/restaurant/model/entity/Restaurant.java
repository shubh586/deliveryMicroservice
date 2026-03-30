package com.foodexpress.restaurant.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name="restaurants")
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String cuisineType;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String state;

    private String postalCode;

    private Double latitude;

    private Double longitude;

    private String phone;

    private String email;

    private String logoUrl;

    private String bannerUrl;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalRatings = 0;

    @Builder.Default
    private Boolean isOpen = true;

    @Builder.Default
    private Boolean isVerified = false;

    @Column(columnDefinition = "TEXT")
    private String openingHours;

    @Builder.Default
    private Integer avgDeliveryTime = 30;

    @Builder.Default
    private Double minOrderAmount = 0.0;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuCategory> menuCategories = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public void addMenuCategory(MenuCategory category) {
        menuCategories.add(category);
        category.setRestaurant(this);
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        item.setRestaurant(this);
    }

}
