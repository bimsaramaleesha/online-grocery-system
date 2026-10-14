package com.grocery.order.strategy.payment;

/**
 * Concrete Strategy: Handles Card Payment on Delivery via portable POS machine.
 */
public class CardOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public String getMethodName() {
        return "Card Payment";
    }

    @Override
    public String generateHandlingInstructions(String orderId, double netAmount) {
        return String.format("Courier dispatch: Provide portable POS terminal to charge Rs. %.2f for order %s.", netAmount, orderId);
    }

    @Override
    public boolean requiresCardTerminal() {
        return true;
    }
}
