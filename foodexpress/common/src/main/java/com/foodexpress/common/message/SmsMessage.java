package com.foodexpress.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Message sent to RabbitMQ for SMS notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {

    public static final String QUEUE = "sms.notifications";
    public static final String EXCHANGE = "notification.exchange";
    public static final String ROUTING_KEY = "notification.sms";


    public static final String OTP = "OTP";
    public static final String ORDER_UPDATE = "ORDER_UPDATE";
    public static final String DELIVERY_UPDATE = "DELIVERY_UPDATE";


    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";
    private String messageId;
    private String type;
    private String to;
    private String phoneNumber;
    private String message;
    private String templateName;
    private Map<String, Object> context;
    private String priority;
    private Instant createdAt;

    public static SmsMessage create(String type, String to, String message) {
        return SmsMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(type)
                .to(to)
                .phoneNumber(to)
                .message(message)
                .priority(PRIORITY_NORMAL)
                .createdAt(Instant.now())
                .build();
    }
}
