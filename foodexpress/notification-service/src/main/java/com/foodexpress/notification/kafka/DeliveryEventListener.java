package com.foodexpress.notification.kafka;


import com.foodexpress.common.events.DeliveryEvent;
import com.foodexpress.common.message.SmsMessage;
import com.foodexpress.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventListener.class);

    private final SmsService smsService;

    @KafkaListener(topics = DeliveryEvent.TOPIC, groupId = "notification-service")
    public void handleDeliveryEvent(DeliveryEvent event) {
        logger.info("Received delivery event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case DeliveryEvent.DELIVERY_ASSIGNED:
                handleDeliveryAssigned(event);
                break;
            case DeliveryEvent.DELIVERY_PICKED_UP:
                handleDeliveryPickedUp(event);
                break;
            default:
                logger.debug("No notification for delivery event type: {}", event.getEventType());
        }
    }

    private void handleDeliveryAssigned(DeliveryEvent event) {
        if (event.getCustomerPhone() != null) {
            SmsMessage sms = SmsMessage.builder()
                    .phoneNumber(event.getCustomerPhone())
                    .templateName("delivery_assigned")
                    .context(Map.of(
                            "orderId", event.getOrderId(),
                            "deliveryPartnerName", event.getDeliveryPartnerName() != null ?
                                    event.getDeliveryPartnerName() : "Partner",
                            "estimatedTime", event.getEstimatedDeliveryMinutes() != null ?
                                    event.getEstimatedDeliveryMinutes() + " minutes" : "30-45 minutes"
                    ))
                    .priority(SmsMessage.PRIORITY_HIGH)
                    .build();
            smsService.sendSms(sms);
        }

        logger.info("Delivery assigned notification sent for order: {}", event.getOrderId());
    }

    private void handleDeliveryPickedUp(DeliveryEvent event) {
        if (event.getCustomerPhone() != null) {
            SmsMessage sms = SmsMessage.builder()
                    .phoneNumber(event.getCustomerPhone())
                    .templateName("delivery_pickup")
                    .context(Map.of(
                            "orderId", event.getOrderId(),
                            "deliveryPartnerName", event.getDeliveryPartnerName() != null ?
                                    event.getDeliveryPartnerName() : "Partner",
                            "deliveryPartnerPhone", event.getDeliveryPartnerPhone() != null ?
                                    event.getDeliveryPartnerPhone() : ""
                    ))
                    .priority(SmsMessage.PRIORITY_HIGH)
                    .build();
            smsService.sendSms(sms);
        }

        logger.info("Delivery pickup notification sent for order: {}", event.getOrderId());
    }
}
