package com.foodexpress.notification.messaging;


import com.foodexpress.common.message.EmailMessage;
import com.foodexpress.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailMessageConsumer.class);

    private final EmailService emailService;

    @RabbitListener(queues = "notification.email")
    public void handleEmailMessage(EmailMessage message) {
        logger.info("Received email message for: {}, subject: {}", message.getTo(), message.getSubject());

        try {
            emailService.sendEmail(message);
        } catch (Exception e) {
            logger.error("Failed to process email message", e);
            // In production, would implement dead-letter queue or retry logic
        }
    }
}
