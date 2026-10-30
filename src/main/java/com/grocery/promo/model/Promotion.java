package com.grocery.promo.model;

import com.grocery.common.FileHandler;

import java.time.LocalDate;

/**
 * Member 5 — Discounts & Promo Codes Model.
 * Demonstrates Encapsulation, Inheritance, and Polymorphism via PercentagePromotion and FixedPromotion subclasses.
 * File format (promotions.txt):
 * promoType|id|name|description|couponCode|discountPercentage|minimumOrderAmount|startDate|expiryDate|active
 */
public class Promotion {

    public static final String TYPE_PERCENTAGE = "PERCENTAGE";
    public static final String TYPE_FIXED = "FIXED";

    private String promoType = TYPE_PERCENTAGE;
    private String id;
    private String name;
    private String description;
    private String couponCode;
    private double discountPercentage;
    private double minimumOrderAmount;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private boolean active = true;

    public Promotion() {
    }

    public Promotion(String promoType, String id, String name, String description,
                     String couponCode, double discountPercentage,
                     double minimumOrderAmount, LocalDate startDate,
                     LocalDate expiryDate, boolean active) {
        this.promoType = (promoType == null || promoType.isBlank()) ? TYPE_PERCENTAGE : promoType.trim().toUpperCase();
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

    /** Polymorphic discount calculation overridden by subclasses. */
    public double calculateDiscount(double orderAmount) {
        if (orderAmount < minimumOrderAmount) {
            return 0.0;
        }
        return Math.min(orderAmount, Math.round(orderAmount * (discountPercentage / 100.0) * 100.0) / 100.0);
    }

    /** Polymorphic human-readable discount badge overridden by subclasses. */
    public String getDiscountDisplay() {
        return discountPercentage + "% OFF";
    }

    /** Subclass 1: Percentage-based discount coupon (Inheritance + Polymorphism). */
    public static class PercentagePromotion extends Promotion {
        public PercentagePromotion(String id, String name, String description, String couponCode,
                                   double discountPercentage, double minimumOrderAmount,
                                   LocalDate startDate, LocalDate expiryDate, boolean active) {
            super(TYPE_PERCENTAGE, id, name, description, couponCode, discountPercentage,
                    minimumOrderAmount, startDate, expiryDate, active);
        }

        @Override
        public double calculateDiscount(double orderAmount) {
            if (orderAmount < getMinimumOrderAmount()) {
                return 0.0;
            }
            return Math.min(orderAmount, Math.round(orderAmount * (getDiscountPercentage() / 100.0) * 100.0) / 100.0);
        }

        @Override
        public String getDiscountDisplay() {
            return getDiscountPercentage() + "% OFF";
        }
    }

    /** Subclass 2: Fixed-amount (Rs.) discount coupon (Inheritance + Polymorphism). */
    public static class FixedPromotion extends Promotion {
        public FixedPromotion(String id, String name, String description, String couponCode,
                              double flatAmount, double minimumOrderAmount,
                              LocalDate startDate, LocalDate expiryDate, boolean active) {
            super(TYPE_FIXED, id, name, description, couponCode, flatAmount,
                    minimumOrderAmount, startDate, expiryDate, active);
        }

        @Override
        public double calculateDiscount(double orderAmount) {
            if (orderAmount < getMinimumOrderAmount()) {
                return 0.0;
            }
            return Math.min(orderAmount, Math.round(getDiscountPercentage() * 100.0) / 100.0);
        }

        @Override
        public String getDiscountDisplay() {
            return "Rs. " + String.format(java.util.Locale.US, "%.2f", getDiscountPercentage()) + " OFF";
        }
    }

    /** Factory method to build the appropriate Promotion subclass. */
    public static Promotion createTyped(Promotion src) {
        if (TYPE_FIXED.equalsIgnoreCase(src.getPromoType())) {
            return new FixedPromotion(src.getId(), src.getName(), src.getDescription(),
                    src.getCouponCode(), src.getDiscountPercentage(), src.getMinimumOrderAmount(),
                    src.getStartDate(), src.getExpiryDate(), src.isActive());
        }
        return new PercentagePromotion(src.getId(), src.getName(), src.getDescription(),
                src.getCouponCode(), src.getDiscountPercentage(), src.getMinimumOrderAmount(),
                src.getStartDate(), src.getExpiryDate(), src.isActive());
    }

    public String toFileLine() {
        return String.join("|",
                FileHandler.clean(promoType),
                FileHandler.clean(id),
                FileHandler.clean(name),
                FileHandler.clean(description),
                FileHandler.clean(couponCode),
                String.valueOf(discountPercentage),
                String.valueOf(minimumOrderAmount),
                startDate == null ? "" : startDate.toString(),
                expiryDate == null ? "" : expiryDate.toString(),
                String.valueOf(active)
        );
    }

    public static Promotion fromFileLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        try {
            String[] f = line.split("\\|", -1);
            if (f.length >= 10 && (TYPE_PERCENTAGE.equalsIgnoreCase(f[0]) || TYPE_FIXED.equalsIgnoreCase(f[0]))) {
                Promotion p = new Promotion(
                        f[0].trim(), f[1].trim(), f[2].trim(), f[3].trim(), f[4].trim(),
                        Double.parseDouble(f[5].trim()), Double.parseDouble(f[6].trim()),
                        f[7].isBlank() ? null : LocalDate.parse(f[7].trim()),
                        f[8].isBlank() ? null : LocalDate.parse(f[8].trim()),
                        Boolean.parseBoolean(f[9].trim()));
                return createTyped(p);
            }
            if (f.length == 9) {
                Promotion p = new Promotion(
                        TYPE_PERCENTAGE, f[0].trim(), f[1].trim(), f[2].trim(), f[3].trim(),
                        Double.parseDouble(f[4].trim()), Double.parseDouble(f[5].trim()),
                        f[6].isBlank() ? null : LocalDate.parse(f[6].trim()),
                        f[7].isBlank() ? null : LocalDate.parse(f[7].trim()),
                        Boolean.parseBoolean(f[8].trim()));
                return createTyped(p);
            }
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    public String getPromoType() {
        return promoType;
    }

    public void setPromoType(String promoType) {
        this.promoType = (promoType == null || promoType.isBlank()) ? TYPE_PERCENTAGE : promoType.trim().toUpperCase();
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
