package com.grocery.order.strategy.discount;

/**
 * Concrete Strategy & Null Object: Represents no discount applied.
 * Eliminates null checks when computing totals.
 */
public class NoDiscountStrategy implements DiscountStrategy {

    public static final NoDiscountStrategy INSTANCE = new NoDiscountStrategy();

    @Override
    public double calculateDiscount(double subtotal) {
        return 0.0;
    }

    @Override
    public String getDescription() {
        return "No Discount Applied";
    }
}
