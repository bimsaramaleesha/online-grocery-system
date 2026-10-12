package com.grocery.order.strategy.discount;

/**
 * Strategy Pattern: Interface for promotional discount calculations.
 * Defines the contract for different algorithmic ways discounts are applied.
 */
public interface DiscountStrategy {

    /**
     * Calculates the monetary discount amount based on order subtotal.
     *
     * @param subtotal the order gross total before discount
     * @return the calculated discount amount in LKR (Rs.)
     */
    double calculateDiscount(double subtotal);

    /**
     * Descriptive label of the applied promotional strategy.
     */
    String getDescription();
}
