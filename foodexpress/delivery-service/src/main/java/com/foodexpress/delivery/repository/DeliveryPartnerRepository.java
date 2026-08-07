package com.foodexpress.delivery.repository;

import com.foodexpress.delivery.model.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, String> {

    Optional<DeliveryPartner> findByPhone(String phone);

    List<DeliveryPartner> findByIsOnlineTrueAndIsAvailableTrueAndIsVerifiedTrue();

    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isOnline = true " +
            "AND dp.isAvailable = true AND dp.isVerified = true " +
            "AND dp.currentLatitude IS NOT NULL AND dp.currentLongitude IS NOT NULL " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(dp.currentLatitude)) " +
            "* cos(radians(dp.currentLongitude) - radians(:lon)) " +
            "+ sin(radians(:lat)) * sin(radians(dp.currentLatitude)))) ASC")
    List<DeliveryPartner> findNearestAvailablePartners(@Param("lat") Double latitude,
                                                       @Param("lon") Double longitude);

    long countByIsOnlineTrue();

    long countByIsOnlineTrueAndIsAvailableTrue();
}
