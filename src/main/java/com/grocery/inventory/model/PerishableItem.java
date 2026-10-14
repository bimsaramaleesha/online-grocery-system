package com.grocery.inventory.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * INHERITANCE: "extends Item" means PerishableItem automatically has id, name,
 * category, price, quantity and all their getters/setters. We only add what is new.
 * Example: milk, vegetables, meat.
 */
public class PerishableItem extends Item {

    public static final String TYPE = "PERISHABLE";
    private static final int SOON_DAYS = 3;
    private static final double SOON_DISCOUNT = 0.20;

    private LocalDate expiryDate;

    public PerishableItem(String id, String name, String category,
                          double price, int quantity, LocalDate expiryDate) {
        super(id, name, category, price, quantity);   // let Item store the common fields
        setExpiryDate(expiryDate);
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public final void setExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date is required for perishable items.");
        }
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean isExpiringSoon() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return daysLeft >= 0 && daysLeft <= SOON_DAYS;
    }

    @Override
    public String getItemType() { return TYPE; }

    @Override
    public String getTypeLabel() { return "Perishable"; }

    /** Items close to expiry are sold 20% cheaper. */
    @Override
    public double getSellingPrice() {
        double result = isExpiringSoon() ? getPrice() * (1 - SOON_DISCOUNT) : getPrice();
        return Math.round(result * 100.0) / 100.0;
    }

    @Override
    public String getDetails() {
        if (isExpired()) {
            return "EXPIRED on " + expiryDate;
        }
        if (isExpiringSoon()) {
            return "Expires soon: " + expiryDate + " (20% off)";
        }
        return "Expires on " + expiryDate;
    }

    @Override
    protected String getExtraFields() {
        return expiryDate.toString();
    }
}
