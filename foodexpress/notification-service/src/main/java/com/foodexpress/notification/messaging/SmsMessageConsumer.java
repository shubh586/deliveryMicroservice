package com.foodexpress.notification.messaging;
import com.foodexpress.common.message.SmsMessage;
import com.foodexpress.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SmsMessageConsumer.class);

    private final SmsService smsService;

    @RabbitListener(queues = "notification.sms")
    public void handleSmsMessage(SmsMessage message) {
        logger.info("Received SMS message for: {}", message.getPhoneNumber());

        try {
            smsService.sendSms(message);
        } catch (Exception e) {
            logger.error("Failed to process SMS message", e);
        }
    }
}

