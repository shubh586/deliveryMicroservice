package com.foodexpress.delivery.kafka;

import com.foodexpress.common.events.DeliveryEvent;
import com.foodexpress.delivery.model.entity.Delivery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDeliveryAssigned(Delivery delivery) {
        DeliveryEvent event = buildEvent(delivery, DeliveryEvent.DELIVERY_ASSIGNED);
        publish(event);
    }

    public void publishDeliveryPickedUp(Delivery delivery) {
        DeliveryEvent event = buildEvent(delivery, DeliveryEvent.DELIVERY_PICKED_UP);
        publish(event);
    }

    public void publishDeliveryCompleted(Delivery delivery) {
        DeliveryEvent event = buildEvent(delivery, DeliveryEvent.DELIVERY_COMPLETED);
        publish(event);
    }

    private DeliveryEvent buildEvent(Delivery delivery, String eventType) {
        DeliveryEvent event = DeliveryEvent.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .customerId(delivery.getCustomerId())
                .customerPhone(delivery.getCustomerPhone())
                .restaurantId(delivery.getRestaurantId())
                .deliveryPartnerId(delivery.getDeliveryPartnerId())
                .deliveryPartnerName(delivery.getDeliveryPartnerName())
                .deliveryPartnerPhone(delivery.getDeliveryPartnerPhone())
                .status(delivery.getStatus())
                .estimatedDeliveryMinutes(delivery.getEstimatedDeliveryMinutes())
                .build();

        event.initializeEvent(eventType);
        return event;
    }

    private void publish(DeliveryEvent event) {
        try {
            kafkaTemplate.send(DeliveryEvent.TOPIC, event.getOrderId(), event);
            logger.info("Published delivery event: type={}, orderId={}, deliveryId={}",
                    event.getEventType(), event.getOrderId(), event.getDeliveryId());
        } catch (Exception e) {
            logger.error("Failed to publish delivery event: type={}, orderId={}",
                    event.getEventType(), event.getOrderId(), e);
        }
    }
}
