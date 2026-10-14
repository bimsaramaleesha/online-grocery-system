package com.grocery.order.strategy.payment;

/**
 * Concrete Strategy: Handles Cash on Delivery (COD).
 */
public class CashOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public String getMethodName() {
        return "Cash on Delivery";
    }

    @Override
    public String generateHandlingInstructions(String orderId, double netAmount) {
        return String.format("Collect Rs. %.2f cash in hand upon delivery of order %s.", netAmount, orderId);
    }

    @Override
    public boolean requiresCardTerminal() {
        return false;
    }
}
