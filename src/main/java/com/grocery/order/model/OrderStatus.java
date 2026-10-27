package com.grocery.order.model;

/**
 * Represents the lifecycle stages of a grocery customer order.
 */
public enum OrderStatus {
    PENDING("Pending", "badge bg-warning text-dark"),
    CONFIRMED("Confirmed", "badge bg-info text-dark"),
    SHIPPED("Shipped", "badge bg-primary"),
    DELIVERED("Delivered", "badge bg-success"),
    CANCELLED("Cancelled", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static OrderStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return PENDING;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return PENDING;
    }
}
