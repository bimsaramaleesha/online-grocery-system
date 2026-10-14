package com.grocery.order.strategy.discount;

/**
 * Concrete Strategy: Calculates a fixed flat deduction, optionally subject
 * to a minimum spend threshold.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    private final double amount;
    private final double minimumSpend;

    public FixedAmountDiscountStrategy(double amount, double minimumSpend) {
        if (amount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.amount = amount;
        this.minimumSpend = Math.max(0.0, minimumSpend);
    }

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal < minimumSpend) {
            return 0.0;
        }
        return Math.min(subtotal, Math.round(amount * 100.0) / 100.0);
    }

    @Override
    public String getDescription() {
        if (minimumSpend > 0) {
            return String.format("Rs. %.2f Flat Off on orders over Rs. %.2f", amount, minimumSpend);
        }
        return String.format("Rs. %.2f Flat Off", amount);
    }

    public double getAmount() {
        return amount;
    }

    public double getMinimumSpend() {
        return minimumSpend;
    }
}
