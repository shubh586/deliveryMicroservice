package com.foodexpress.delivery.service;

import com.foodexpress.common.enums.DeliveryStatus;
import com.foodexpress.delivery.kafka.DeliveryEventPublisher;
import com.foodexpress.delivery.model.entity.Delivery;
import com.foodexpress.delivery.model.entity.DeliveryPartner;
import com.foodexpress.delivery.repository.DeliveryPartnerRepository;
import com.foodexpress.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryAssignmentService.class);

    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventPublisher eventPublisher;


    @Transactional
    public boolean assignDeliveryPartner(Delivery delivery) {
        logger.info("Attempting to assign delivery partner for order: {}", delivery.getOrderId());

        // find nearest available partners
        List<DeliveryPartner> availablePartners;

        if (delivery.getPickupLatitude() != null && delivery.getPickupLongitude() != null) {
            availablePartners = partnerRepository.findNearestAvailablePartners(
                    delivery.getPickupLatitude(),
                    delivery.getPickupLongitude()
            );
        } else {
            availablePartners = partnerRepository.findByIsOnlineTrueAndIsAvailableTrueAndIsVerifiedTrue();
        }

        if (availablePartners.isEmpty()) {
            logger.warn("No available delivery partners for order: {}", delivery.getOrderId());
            return false;
        }

        // assign to the first (nearest) available partner
        DeliveryPartner partner = availablePartners.get(0);

        // update partner status
        partner.setIsAvailable(false);
        partner.setCurrentOrderId(delivery.getOrderId());
        partnerRepository.save(partner);

        // update delivery
        delivery.setDeliveryPartnerId(partner.getId());
        delivery.setDeliveryPartnerName(partner.getName());
        delivery.setDeliveryPartnerPhone(partner.getPhone());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(Instant.now());
        delivery.setEstimatedPickupMinutes(10); // would calculate based on distance
        delivery.setEstimatedDeliveryMinutes(25);
        deliveryRepository.save(delivery);

        // Publish delivery assigned event to Kafka
        eventPublisher.publishDeliveryAssigned(delivery);

        logger.info("Delivery partner {} assigned to order {}", partner.getId(), delivery.getOrderId());
        return true;
    }

    @Transactional
    public void releaseDeliveryPartner(String partnerId) {
        partnerRepository.findById(partnerId).ifPresent(partner -> {
            partner.setIsAvailable(true);
            partner.setCurrentOrderId(null);
            partnerRepository.save(partner);
            logger.info("Delivery partner {} released", partnerId);
        });
    }
}

