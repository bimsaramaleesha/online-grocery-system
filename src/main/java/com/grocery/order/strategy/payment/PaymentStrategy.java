package com.grocery.order.strategy.payment;

/**
 * Strategy Pattern: Interface for processing and managing payment methods.
 */
public interface PaymentStrategy {

    /**
     * Display name of the payment mode.
     */
    String getMethodName();

    /**
     * Verifies and generates handling instructions for the courier.
     */
    String generateHandlingInstructions(String orderId, double netAmount);

    /**
     * Indicates whether courier must bring a card terminal.
     */
    boolean requiresCardTerminal();
}
