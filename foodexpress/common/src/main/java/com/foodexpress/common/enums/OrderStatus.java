package com.foodexpress.common.enums;

public enum OrderStatus {
    PENDING,
    PLACED,
    CONFIRMED,
    PREPARING,
    READY,
    READY_FOR_PICKUP,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    /**
     * Check if the order can be cancelled in its current status
     */
    public boolean canCancel() {
        return this == PENDING || this == PLACED || this == CONFIRMED || this == PREPARING;
    }
}
