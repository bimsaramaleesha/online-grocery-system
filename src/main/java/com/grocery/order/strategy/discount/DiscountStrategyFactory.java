package com.grocery.order.strategy.discount;

import com.grocery.common.FileHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory Pattern: Resolves and creates the appropriate DiscountStrategy
 * based on user promo code input or persisted promo records.
 */
@Component
public class DiscountStrategyFactory {

    /**
     * Resolves the proper discount strategy for a given promotional code.
     *
     * @param promoCode the promotional string entered by user
     * @return the resolved DiscountStrategy (never null)
     */
    public DiscountStrategy getStrategy(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return NoDiscountStrategy.INSTANCE;
        }

        String code = promoCode.trim().toUpperCase();

        // 1. Try reading live promos.txt if configured
        try {
            List<String> lines = FileHandler.readLines("promos.txt");
            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    String fileCode = parts[1].trim().toUpperCase();
                    if (fileCode.equals(code)) {
                        String type = parts[2].trim();
                        double val = Double.parseDouble(parts[3].trim());
                        double minSpend = parts.length > 4 ? Double.parseDouble(parts[4].trim()) : 0.0;

                        if ("PERCENTAGE".equalsIgnoreCase(type) || type.contains("%")) {
                            return new PercentageDiscountStrategy(val);
                        } else {
                            return new FixedAmountDiscountStrategy(val, minSpend);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Graceful fallback to demo promotional strategies
        }

        // 2. Demo promotional rules
        switch (code) {
            case "WELCOME10":
                return new PercentageDiscountStrategy(10.0); // 10% off
            case "GROCERY5":
                return new PercentageDiscountStrategy(5.0); // 5% off
            case "SAVE200":
                return new FixedAmountDiscountStrategy(200.0, 1000.0); // Rs. 200 off on 1000+
            case "FLAT100":
                return new FixedAmountDiscountStrategy(100.0, 500.0); // Rs. 100 off on 500+
            default:
                return NoDiscountStrategy.INSTANCE;
        }
    }
}
