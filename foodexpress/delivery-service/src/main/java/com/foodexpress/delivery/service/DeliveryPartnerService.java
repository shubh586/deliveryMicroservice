package com.foodexpress.delivery.service;

import com.foodexpress.common.exception.BadRequestException;
import com.foodexpress.common.exception.ResourceNotFoundException;
import com.foodexpress.delivery.model.dto.DeliveryRequest;
import com.foodexpress.delivery.model.dto.DeliveryResponse;
import com.foodexpress.delivery.model.entity.DeliveryPartner;
import com.foodexpress.delivery.repository.DeliveryPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryPartnerService.class);

    private final DeliveryPartnerRepository partnerRepository;

    @Transactional
    public DeliveryResponse.PartnerInfo registerPartner(String userId, DeliveryRequest.RegisterPartner request) {
        if (partnerRepository.existsById(userId)) {
            throw new BadRequestException("Partner already registered");
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .id(userId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .profilePicture(request.getProfilePicture())
                .isVerified(false) // Admin needs to verify
                .isOnline(false)
                .isAvailable(false)
                .build();

        partner = partnerRepository.save(partner);
        logger.info("Delivery partner registered: {}", userId);

        return toPartnerInfo(partner);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse.PartnerInfo getPartnerProfile(String partnerId) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));
        return toPartnerInfo(partner);
    }

    @Transactional
    public DeliveryResponse.PartnerInfo goOnline(String partnerId) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        if (!partner.getIsVerified()) {
            throw new BadRequestException("Account not verified yet");
        }

        partner.setIsOnline(true);
        partner.setIsAvailable(partner.getCurrentOrderId() == null);
        partner = partnerRepository.save(partner);

        logger.info("Partner {} went online", partnerId);
        return toPartnerInfo(partner);
    }

    @Transactional
    public DeliveryResponse.PartnerInfo goOffline(String partnerId) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        if (partner.getCurrentOrderId() != null) {
            throw new BadRequestException("Cannot go offline with active delivery");
        }

        partner.setIsOnline(false);
        partner.setIsAvailable(false);
        partner = partnerRepository.save(partner);

        logger.info("Partner {} went offline", partnerId);
        return toPartnerInfo(partner);
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

    // Admin methods

    @Transactional
    public DeliveryResponse.PartnerInfo verifyPartner(String partnerId) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryPartner", "id", partnerId));

        partner.setIsVerified(true);
        partner = partnerRepository.save(partner);

        logger.info("Partner {} verified", partnerId);
        return toPartnerInfo(partner);
    }

    private DeliveryResponse.PartnerInfo toPartnerInfo(DeliveryPartner partner) {
        return DeliveryResponse.PartnerInfo.builder()
                .id(partner.getId())
                .name(partner.getName())
                .phone(partner.getPhone())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .profilePicture(partner.getProfilePicture())
                .isAvailable(partner.getIsAvailable())
                .isOnline(partner.getIsOnline())
                .isVerified(partner.getIsVerified())
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .totalDeliveries(partner.getTotalDeliveries())
                .rating(partner.getRating())
                .currentOrderId(partner.getCurrentOrderId())
                .createdAt(partner.getCreatedAt())
                .build();
    }
}

