 package com.grocery.promo.service;

import com.grocery.promo.model.Promotion;
import com.grocery.promo.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PromotionService {

    private final PromotionRepository repository;

    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
    }

    public List<Promotion> getAllPromotions() {
        return repository.findAll();
    }

    public List<Promotion> getActivePromotions() {
        LocalDate today = LocalDate.now();

        return repository.findAll().stream()
                .filter(Promotion::isActive)
                .filter(p -> p.getStartDate() == null
                        || !p.getStartDate().isAfter(today))
                .filter(p -> p.getExpiryDate() == null
                        || !p.getExpiryDate().isBefore(today))
                .toList();
    }

    public Optional<Promotion> getPromotionById(String id) {
        return repository.findById(id);
    }

    public Promotion createPromotion(Promotion promotion) {
        validatePromotion(promotion);

        promotion.setId(null);
        promotion.setName(clean(promotion.getName()));
        promotion.setDescription(clean(promotion.getDescription()));
        promotion.setCouponCode(clean(promotion.getCouponCode())
                .toUpperCase(Locale.ROOT));

        if (repository.findByCouponCodeIgnoreCase(
                promotion.getCouponCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "A promotion with this coupon code already exists.");
        }

        return repository.save(promotion);
    }

    public Promotion updatePromotion(String id, Promotion promotion) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Promotion not found: " + id);
        }

        validatePromotion(promotion);

        Optional<Promotion> duplicate =
                repository.findByCouponCodeIgnoreCase(
                        clean(promotion.getCouponCode()));

        if (duplicate.isPresent()
                && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "A promotion with this coupon code already exists.");
        }

        promotion.setId(id);
        promotion.setName(clean(promotion.getName()));
        promotion.setDescription(clean(promotion.getDescription()));
        promotion.setCouponCode(clean(promotion.getCouponCode())
                .toUpperCase(Locale.ROOT));

        return repository.save(promotion);
    }

    public void deletePromotion(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Promotion not found: " + id);
        }

        repository.deleteById(id);
    }

    public Optional<Promotion> findPromotionByCouponCode(String couponCode) {
        return repository.findByCouponCodeIgnoreCase(clean(couponCode));
    }

    public boolean isCouponValid(String couponCode, double orderAmount) {
        if (couponCode == null || couponCode.isBlank() || orderAmount < 0) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return repository.findByCouponCodeIgnoreCase(
                        clean(couponCode).toUpperCase(Locale.ROOT))
                .filter(Promotion::isActive)
                .filter(p -> p.getStartDate() == null
                        || !today.isBefore(p.getStartDate()))
                .filter(p -> p.getExpiryDate() == null
                        || !today.isAfter(p.getExpiryDate()))
                .filter(p -> orderAmount >= p.getMinimumOrderAmount())
                .filter(p -> p.getDiscountPercentage() > 0
                        && p.getDiscountPercentage() <= 100)
                .isPresent();
    }

    public double calculateDiscount(String couponCode, double orderAmount) {
        if (!isCouponValid(couponCode, orderAmount)) {
            return 0.0;
        }

        Promotion promotion = repository
                .findByCouponCodeIgnoreCase(clean(couponCode))
                .orElseThrow();

        return orderAmount * promotion.getDiscountPercentage() / 100.0;
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be empty.");
        }

        if (promotion.getName() == null
                || promotion.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Promotion name is required.");
        }

        if (promotion.getCouponCode() == null
                || promotion.getCouponCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Coupon code is required.");
        }

        if (promotion.getDiscountPercentage() <= 0
                || promotion.getDiscountPercentage() > 100) {
            throw new IllegalArgumentException(
                    "Discount must be between 1 and 100 percent.");
        }

        if (!Double.isFinite(promotion.getMinimumOrderAmount())
                || promotion.getMinimumOrderAmount() < 0) {
            throw new IllegalArgumentException(
                    "Minimum order amount must be a valid non-negative amount.");
        }

        if (promotion.getStartDate() != null
                && promotion.getExpiryDate() != null
                && promotion.getExpiryDate()
                .isBefore(promotion.getStartDate())) {
            throw new IllegalArgumentException(
                    "Expiry date cannot be before the start date.");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

