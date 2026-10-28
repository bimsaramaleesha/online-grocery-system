package com.grocery.order.strategy.discount;

/**
 * Concrete Strategy: Calculates discount as a percentage of the subtotal,
 * optionally subject to a minimum spend threshold.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final double percentage;
    private final double minimumSpend;

    public PercentageDiscountStrategy(double percentage) {
        this(percentage, 0.0);
    }

    public PercentageDiscountStrategy(double percentage, double minimumSpend) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
        this.minimumSpend = Math.max(0.0, minimumSpend);
    }

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal <= 0 || subtotal < minimumSpend) {
            return 0.0;
        }
        double discount = subtotal * (percentage / 100.0);
        return Math.round(discount * 100.0) / 100.0;
    }

    @Override
    public String getDescription() {
        if (minimumSpend > 0) {
            return String.format("%.0f%% Off Discount on orders over Rs. %.2f", percentage, minimumSpend);
        }
        return String.format("%.0f%% Off Discount", percentage);
    }

    public double getPercentage() {
        return percentage;
    }

    public double getMinimumSpend() {
        return minimumSpend;
    }
}
