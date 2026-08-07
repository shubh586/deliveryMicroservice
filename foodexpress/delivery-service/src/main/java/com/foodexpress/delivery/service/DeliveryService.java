package com.foodexpress.delivery.service;

import com.foodexpress.common.enums.DeliveryStatus;
import com.foodexpress.common.exception.BadRequestException;
import com.foodexpress.common.exception.ResourceNotFoundException;
import com.foodexpress.delivery.model.dto.DeliveryResponse;
import com.foodexpress.delivery.model.entity.Delivery;
import com.foodexpress.delivery.model.entity.DeliveryPartner;
import com.foodexpress.delivery.repository.DeliveryPartnerRepository;
import com.foodexpress.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentService assignmentService;

    @Transactional(readOnly = true)
    public DeliveryResponse.DeliveryInfo getDeliveryByOrderId(String orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        return toDeliveryInfo(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse.DeliveryInfo> getActiveDeliveriesForPartner(String partnerId) {
        List<DeliveryStatus> activeStatuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.IN_TRANSIT
        );

        return deliveryRepository.findByDeliveryPartnerIdAndStatusIn(partnerId, activeStatuses).stream()
                .map(this::toDeliveryInfo)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DeliveryResponse.DeliveryInfo> getDeliveryHistoryForPartner(String partnerId, Pageable pageable) {
        return deliveryRepository.findByDeliveryPartnerIdOrderByCreatedAtDesc(partnerId, pageable)
                .map(this::toDeliveryInfo);
    }

    // Delivery Partner Actions

    @Transactional
    public DeliveryResponse.DeliveryInfo acceptDelivery(String deliveryId, String partnerId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        if (!partnerId.equals(delivery.getDeliveryPartnerId())) {
            throw new BadRequestException("This delivery is not assigned to you");
        }

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BadRequestException("Delivery cannot be accepted in current status");
        }

        // Status remains ASSIGNED, but this confirms the partner saw it
        logger.info("Delivery {} accepted by partner {}", deliveryId, partnerId);

        return toDeliveryInfo(delivery);
    }

    @Transactional
    public DeliveryResponse.DeliveryInfo pickupOrder(String deliveryId, String partnerId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        if (!partnerId.equals(delivery.getDeliveryPartnerId())) {
            throw new BadRequestException("This delivery is not assigned to you");
        }

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BadRequestException("Order must be in ASSIGNED status to pick up");
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(Instant.now());
        delivery = deliveryRepository.save(delivery);

        logger.info("Order picked up for delivery: {}", deliveryId);

        return toDeliveryInfo(delivery);
    }

    @Transactional
    public DeliveryResponse.DeliveryInfo startDelivery(String deliveryId, String partnerId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        if (!partnerId.equals(delivery.getDeliveryPartnerId())) {
            throw new BadRequestException("This delivery is not assigned to you");
        }

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new BadRequestException("Order must be picked up before starting delivery");
        }

        delivery.setStatus(DeliveryStatus.IN_TRANSIT);
        delivery = deliveryRepository.save(delivery);

        logger.info("Delivery started: {}", deliveryId);

        return toDeliveryInfo(delivery);
    }

    @Transactional
    public DeliveryResponse.DeliveryInfo completeDelivery(String deliveryId, String partnerId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        if (!partnerId.equals(delivery.getDeliveryPartnerId())) {
            throw new BadRequestException("This delivery is not assigned to you");
        }

        if (delivery.getStatus() != DeliveryStatus.IN_TRANSIT &&
                delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new BadRequestException("Delivery cannot be completed in current status");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(Instant.now());
        delivery = deliveryRepository.save(delivery);

        // Update partner stats and release
        DeliveryPartner partner = partnerRepository.findById(partnerId).orElse(null);
        if (partner != null) {
            partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
            partner.setIsAvailable(true);
            partner.setCurrentOrderId(null);
            partnerRepository.save(partner);
        }
        logger.info("Delivery completed: {}", deliveryId);

        return toDeliveryInfo(delivery);
    }

    @Transactional
    public void updateLocation(String partnerId, Double latitude, Double longitude) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        partner.setCurrentLatitude(latitude);
        partner.setCurrentLongitude(longitude);
        partner.setLocationUpdatedAt(Instant.now());
        partnerRepository.save(partner);

    }

    private DeliveryResponse.DeliveryInfo toDeliveryInfo(Delivery delivery) {
        return DeliveryResponse.DeliveryInfo.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .customerId(delivery.getCustomerId())
                .customerName(delivery.getCustomerName())
                .customerPhone(delivery.getCustomerPhone())
                .restaurantId(delivery.getRestaurantId())
                .restaurantName(delivery.getRestaurantName())
                .deliveryPartnerId(delivery.getDeliveryPartnerId())
                .deliveryPartnerName(delivery.getDeliveryPartnerName())
                .deliveryPartnerPhone(delivery.getDeliveryPartnerPhone())
                .status(delivery.getStatus())
                .pickupAddress(delivery.getPickupAddress())
                .pickupLatitude(delivery.getPickupLatitude())
                .pickupLongitude(delivery.getPickupLongitude())
                .deliveryAddress(delivery.getDeliveryAddress())
                .deliveryLatitude(delivery.getDeliveryLatitude())
                .deliveryLongitude(delivery.getDeliveryLongitude())
                .deliveryInstructions(delivery.getDeliveryInstructions())
                .estimatedPickupMinutes(delivery.getEstimatedPickupMinutes())
                .estimatedDeliveryMinutes(delivery.getEstimatedDeliveryMinutes())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .customerRating(delivery.getCustomerRating())
                .createdAt(delivery.getCreatedAt())
                .build();
    }
}
