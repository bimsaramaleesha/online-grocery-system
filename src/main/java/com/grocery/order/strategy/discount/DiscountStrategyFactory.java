package com.grocery.order.strategy.discount;

import com.grocery.common.FileHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
            LocalDate today = LocalDate.now();
            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                // Format written by PromotionRepository:
                // id|name|description|couponCode|discountPercentage|minimumOrderAmount|startDate|expiryDate|active
                if (parts.length == 9) {
                    String fileCode = parts[3].trim().toUpperCase();
                    if (fileCode.equals(code)) {
                        boolean active = Boolean.parseBoolean(parts[8].trim());
                        LocalDate start = parts[6].isBlank() ? null : LocalDate.parse(parts[6].trim());
                        LocalDate expiry = parts[7].isBlank() ? null : LocalDate.parse(parts[7].trim());
                        if (!active
                                || (start != null && today.isBefore(start))
                                || (expiry != null && today.isAfter(expiry))) {
                            return NoDiscountStrategy.INSTANCE;
                        }
                        double pct = Double.parseDouble(parts[4].trim());
                        double minSpend = Double.parseDouble(parts[5].trim());
                        return new PercentageDiscountStrategy(pct, minSpend);
                    }
                } else if (parts.length >= 4) {
                    // Format: id|code|type|value|minSpend
                    String fileCode = parts[1].trim().toUpperCase();
                    if (fileCode.equals(code)) {
                        String type = parts[2].trim();
                        double val = Double.parseDouble(parts[3].trim());
                        double minSpend = parts.length > 4 ? Double.parseDouble(parts[4].trim()) : 0.0;

                        if ("PERCENTAGE".equalsIgnoreCase(type) || type.contains("%")) {
                            return new PercentageDiscountStrategy(val, minSpend);
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
