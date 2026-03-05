package com.foodexpress.common.events;

import com.foodexpress.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when delivery status changes
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeliveryEvent extends BaseEvent {

    public static final String TOPIC = "delivery-events";

    // Event types
    public static final String DELIVERY_ASSIGNED = "DELIVERY_ASSIGNED";
    public static final String DELIVERY_PICKED_UP = "DELIVERY_PICKED_UP";
    public static final String DELIVERY_COMPLETED = "DELIVERY_COMPLETED";
    public static final String LOCATION_UPDATED = "LOCATION_UPDATED";

    private String deliveryId;
    private String orderId;
    private String customerId;
    private String customerPhone;
    private String restaurantId;
    private String deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private DeliveryStatus status;
    private Integer estimatedDeliveryMinutes;
    private Double currentLatitude;
    private Double currentLongitude;
}

