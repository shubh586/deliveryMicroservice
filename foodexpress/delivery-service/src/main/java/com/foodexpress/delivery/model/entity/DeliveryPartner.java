package com.foodexpress.delivery.model.entity;
import com.foodexpress.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    private String id; // same as user id

    @Column(nullable = false)
    private String name;

    private String email;

    @Column(nullable = false)
    private String phone;

    private String vehicleType; //bike, scooter,car

    private String vehicleNumber;

    private String profilePicture;

    @Builder.Default
    private Boolean isAvailable = false;

    @Builder.Default
    private Boolean isVerified = false;

    @Builder.Default
    private Boolean isOnline = false;

    // Current location
    private Double currentLatitude;
    private Double currentLongitude;
    private Instant locationUpdatedAt;

    // Statistics
    @Builder.Default
    private Integer totalDeliveries = 0;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalRatings = 0;

    // currently assigned order
    private String currentOrderId;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
