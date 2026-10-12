package com.grocery.order.strategy.payment;

import org.springframework.stereotype.Component;

/**
 * Factory Pattern: Instantiates the appropriate PaymentStrategy.
 */
@Component
public class PaymentStrategyFactory {

    private final CashOnDeliveryStrategy codStrategy = new CashOnDeliveryStrategy();
    private final CardOnDeliveryStrategy cardStrategy = new CardOnDeliveryStrategy();

    /**
     * Factory method to resolve payment strategy by selected name.
     */
    public PaymentStrategy getStrategy(String paymentMethod) {
        if (paymentMethod != null && paymentMethod.toLowerCase().contains("card")) {
            return cardStrategy;
        }
        return codStrategy;
    }
}
