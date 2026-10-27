package com.grocery.order.strategy.discount;

/**
 * Concrete Strategy: Calculates discount as a percentage of the subtotal.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final double percentage;

    public PercentageDiscountStrategy(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        double discount = subtotal * (percentage / 100.0);
        return Math.round(discount * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        return String.format("%.0f%% Off Discount", percentage);
    }

    public double getPercentage() {
        return percentage;
    }
}
