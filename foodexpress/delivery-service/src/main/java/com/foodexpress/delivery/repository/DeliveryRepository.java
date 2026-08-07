package com.foodexpress.delivery.repository;

import com.foodexpress.common.enums.DeliveryStatus;
import com.foodexpress.delivery.model.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    Optional<Delivery> findByOrderId(String orderId);

    Page<Delivery> findByDeliveryPartnerIdOrderByCreatedAtDesc(String deliveryPartnerId, Pageable pageable);

    List<Delivery> findByDeliveryPartnerIdAndStatusIn(String deliveryPartnerId, List<DeliveryStatus> statuses);

    Page<Delivery> findByStatusOrderByCreatedAtAsc(DeliveryStatus status, Pageable pageable);

    long countByDeliveryPartnerIdAndStatus(String deliveryPartnerId, DeliveryStatus status);
}
