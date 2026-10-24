 package com.grocery.promo.model;

import java.time.LocalDate;

/**
 * Represents a grocery store promotion or discount coupon.
 */
public class Promotion {

    private String id;
    private String name;
    private String description;
    private String couponCode;
    private double discountPercentage;
    private double minimumOrderAmount;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean active;

    public Promotion() {
    }

    public Promotion(String id, String name, String description,
                     String couponCode, double discountPercentage,
                     double minimumOrderAmount, LocalDate startDate,
                     LocalDate expiryDate, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.couponCode = couponCode;
        this.discountPercentage = discountPercentage;
        this.minimumOrderAmount = minimumOrderAmount;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(double minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

