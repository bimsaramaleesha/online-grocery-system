package com.grocery.auth;

/**
 * The two user levels of the system.
 *   CUSTOMER = shops only: browse products, cart, place and track orders.
 *   ADMIN    = sees and manages everything (dashboard, inventory, suppliers, all orders ...).
 * To add a new level later (e.g. DELIVERY_STAFF) add it here and give it rules in AccessInterceptor.
 */
public enum Role {
    CUSTOMER("Customer"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
