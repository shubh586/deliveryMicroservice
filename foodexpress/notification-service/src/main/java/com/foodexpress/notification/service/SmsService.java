package com.foodexpress.notification.service;

import com.foodexpress.common.message.SmsMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${sms.provider:console}")
    private String smsProvider;

    /**
     * Send SMS using configured provider
     * For now, this logs to console. In production, integrate with Twilio, AWS SNS, etc.
     */
    public void sendSms(SmsMessage message) {
        String content = buildSmsContent(message.getTemplateName(), message.getContext());

        switch (smsProvider) {
            case "twilio":
                sendViaTwilio(message.getPhoneNumber(), content);
                break;
            case "console":
            default:
                logSms(message.getPhoneNumber(), content);
        }
    }

    private String buildSmsContent(String templateName, Map<String, Object> context) {
        return switch (templateName) {
            case "order_confirmed" -> String.format(
                    "Your order %s has been confirmed! Estimated delivery: %s",
                    context.get("orderId"),
                    context.get("estimatedTime")
            );
            case "order_ready" -> String.format(
                    "Your order %s is ready for pickup!",
                    context.get("orderId")
            );
            case "delivery_pickup" -> String.format(
                    "Your order %s has been picked up by %s. Contact: %s",
                    context.get("orderId"),
                    context.get("deliveryPartnerName"),
                    context.get("deliveryPartnerPhone")
            );
            case "order_delivered" -> String.format(
                    "Your order %s has been delivered. Enjoy your meal!",
                    context.get("orderId")
            );
            case "otp" -> String.format(
                    "Your FoodExpress OTP is: %s. Valid for 5 minutes.",
                    context.get("otp")
            );
            default -> String.format(
                    "FoodExpress: %s",
                    context.getOrDefault("message", "You have a new notification")
            );
        };
    }

    private void sendViaTwilio(String phoneNumber, String content) {
        // TODO: Implement Twilio integration
        // TwilioClient.sendSms(phoneNumber, content);
        logger.info("SMS sent via Twilio to {}: {}", phoneNumber, content);
    }

    private void logSms(String phoneNumber, String content) {
        logger.info("SMS [{}]: {}", phoneNumber, content);
    }
}

