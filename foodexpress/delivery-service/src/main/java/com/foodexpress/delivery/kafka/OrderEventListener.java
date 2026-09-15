package com.foodexpress.delivery.kafka;

import com.foodexpress.common.enums.DeliveryStatus;
import com.foodexpress.common.events.OrderEvent;
import com.foodexpress.delivery.model.entity.Delivery;
import com.foodexpress.delivery.repository.DeliveryRepository;
import com.foodexpress.delivery.service.DeliveryAssignmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    private final DeliveryRepository deliveryRepository;
    private final DeliveryAssignmentService assignmentService;

    @KafkaListener(topics = OrderEvent.TOPIC, groupId = "delivery-service")
    public void handleOrderEvent(OrderEvent event) {
        logger.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        try {
            switch (event.getEventType()) {
                case OrderEvent.ORDER_READY:
                    handleOrderReady(event);
                    break;
                case OrderEvent.ORDER_CANCELLED:
                    handleOrderCancelled(event);
                    break;
                default:
                    logger.debug("Ignoring order event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing order event: type={}, orderId={}",
                    event.getEventType(), event.getOrderId(), e);
        }
    }

    private void handleOrderReady(OrderEvent event) {
        // Check if a delivery already exists for this order
        Optional<Delivery> existing = deliveryRepository.findByOrderId(event.getOrderId());
        if (existing.isPresent()) {
            logger.warn("Delivery already exists for order: {}, skipping", event.getOrderId());
            return;
        }

        // Create a new delivery record from the order event
        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .customerName(event.getCustomerName())
                .customerPhone(event.getCustomerPhone())
                .restaurantId(event.getRestaurantId())
                .restaurantName(event.getRestaurantName())
                .deliveryAddress(event.getDeliveryAddress())
                .deliveryLatitude(event.getDeliveryLatitude())
                .deliveryLongitude(event.getDeliveryLongitude())
                .status(DeliveryStatus.PENDING)
                .build();

        delivery = deliveryRepository.save(delivery);
        logger.info("Delivery created for order: {}, deliveryId: {}", event.getOrderId(), delivery.getId());

        // Attempt to assign a delivery partner
        boolean assigned = assignmentService.assignDeliveryPartner(delivery);
        if (!assigned) {
            logger.warn("No delivery partner available for order: {}. Delivery remains PENDING.", event.getOrderId());
        }
    }

    private void handleOrderCancelled(OrderEvent event) {
        Optional<Delivery> existing = deliveryRepository.findByOrderId(event.getOrderId());
        if (existing.isEmpty()) {
            logger.debug("No delivery found for cancelled order: {}", event.getOrderId());
            return;
        }

        Delivery delivery = existing.get();

        // Only cancel if the delivery is still in a cancellable state
        if (delivery.getStatus() == DeliveryStatus.PENDING ||
                delivery.getStatus() == DeliveryStatus.ASSIGNED) {

            // Release the partner if one was assigned
            if (delivery.getDeliveryPartnerId() != null) {
                assignmentService.releaseDeliveryPartner(delivery.getDeliveryPartnerId());
            }

            delivery.setStatus(DeliveryStatus.CANCELLED);
            delivery.setCancelledAt(Instant.now());
            delivery.setCancellationReason("Order cancelled: " +
                    (event.getCancellationReason() != null ? event.getCancellationReason() : "No reason provided"));
            deliveryRepository.save(delivery);

            logger.info("Delivery cancelled for order: {}", event.getOrderId());
        } else {
            logger.warn("Delivery for order {} is in status {} and cannot be cancelled",
                    event.getOrderId(), delivery.getStatus());
        }
    }
}
