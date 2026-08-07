package com.foodexpress.delivery.model.entity;

import com.foodexpress.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String orderId;

    private String customerId;
    private String customerName;
    private String customerPhone;

    private String restaurantId;
    private String restaurantName;

    private String deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    // Pickup location (restaurant)
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    // Delivery location
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddress;
    private String deliveryInstructions;

    // Delivery fee
    private BigDecimal deliveryFee;
    private BigDecimal tip;

    // Estimated times
    private Integer estimatedPickupMinutes;
    private Integer estimatedDeliveryMinutes;

    // Actual timestamps
    private Instant assignedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant cancelledAt;

    private String cancellationReason;

    // Rating
    private Integer customerRating;
    private String customerFeedback;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
