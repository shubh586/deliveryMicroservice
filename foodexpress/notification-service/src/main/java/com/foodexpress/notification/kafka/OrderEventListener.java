package com.foodexpress.notification.kafka;


import com.foodexpress.common.events.OrderEvent;
import com.foodexpress.common.message.SmsMessage;
import com.foodexpress.notification.service.EmailService;
import com.foodexpress.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    private final EmailService emailService;
    private final SmsService smsService;

    @KafkaListener(topics = OrderEvent.TOPIC, groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event) {
        logger.info("Received order event: type={}, orderId={}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case OrderEvent.ORDER_PLACED:
                handleOrderPlaced(event);
                break;
            case OrderEvent.ORDER_CONFIRMED:
                handleOrderConfirmed(event);
                break;
            case OrderEvent.ORDER_DELIVERED:
                handleOrderDelivered(event);
                break;
            case OrderEvent.ORDER_CANCELLED:
                handleOrderCancelled(event);
                break;
            default:
                logger.debug("No notification for event type: {}", event.getEventType());
        }
    }

    private void handleOrderPlaced(OrderEvent event) {
        // Send confirmation email would be sent here
        logger.info("Order placed notification handled for order: {}", event.getOrderId());
    }

    private void handleOrderConfirmed(OrderEvent event) {
        // SMS notification to customer
        if (event.getCustomerPhone() != null) {
            SmsMessage sms = SmsMessage.builder()
                    .phoneNumber(event.getCustomerPhone())
                    .templateName("order_confirmed")
                    .context(Map.of(
                            "orderId", event.getOrderId(),
                            "estimatedTime", "30-45 minutes"))
                    .priority(SmsMessage.PRIORITY_HIGH)
                    .build();
            smsService.sendSms(sms);
        }

        logger.info("Order confirmed notification sent for order: {}", event.getOrderId());
    }

    private void handleOrderDelivered(OrderEvent event) {
        // Thank you message
        if (event.getCustomerPhone() != null) {
            SmsMessage sms = SmsMessage.builder()
                    .phoneNumber(event.getCustomerPhone())
                    .templateName("order_delivered")
                    .context(Map.of("orderId", event.getOrderId()))
                    .priority(SmsMessage.PRIORITY_NORMAL)
                    .build();
            smsService.sendSms(sms);
        }

        logger.info("Order delivered notification sent for order: {}", event.getOrderId());
    }

    private void handleOrderCancelled(OrderEvent event) {
        // Cancellation notification
        logger.info("Order cancelled notification for order: {}", event.getOrderId());
    }
}
