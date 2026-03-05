package com.foodexpress.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    public static final String QUEUE = "email.notifications";
    public static final String EXCHANGE = "notification.exchange";
    public static final String ROUTING_KEY = "notification.email";


    public static final String ORDER_CONFIRMATION = "ORDER_CONFIRMATION";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
    public static final String WELCOME = "WELCOME";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";
    public static final String OTP = "OTP";


    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";

    private String messageId;
    private String type;
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
    private Map<String, Object> context;
    private String priority;
    private Instant createdAt;

    public static EmailMessage create(String type, String to, String subject) {
        return EmailMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .type(type)
                .to(to)
                .subject(subject)
                .priority(PRIORITY_NORMAL)
                .createdAt(Instant.now())
                .build();
    }
}
