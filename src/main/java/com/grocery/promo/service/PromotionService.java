
package com.grocery.promo.service;

import com.grocery.promo.model.Promotion;
import com.grocery.promo.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Contains business logic for promotions and coupons
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
        return repository.findByActiveTrue();
    }

    public Optional<Promotion> getPromotionById(Long id) {
        return repository.findById(id);
    }

    public Optional<Promotion> getByCouponCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return repository.findByCouponCodeIgnoreCase(
                code.trim());
    }

    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        validatePromotion(promotion);

        promotion.setCouponCode(
                promotion.getCouponCode().trim());

        if (getByCouponCode(
                promotion.getCouponCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "Coupon code already exists");
        }

        return repository.save(promotion);
    }

    @Transactional
    public Promotion updatePromotion(
            Long id, Promotion updated) {

        Promotion existing = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Promotion not found"));

        validatePromotion(updated);

        Optional<Promotion> duplicate =
                getByCouponCode(updated.getCouponCode());

        if (duplicate.isPresent()
                && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException(
                    "Coupon code already exists");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCouponCode(
                updated.getCouponCode().trim());
        existing.setDiscountPercentage(
                updated.getDiscountPercentage());
        existing.setMinimumOrderAmount(
                updated.getMinimumOrderAmount());
        existing.setStartDate(updated.getStartDate());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setActive(updated.isActive());

        return repository.save(existing);
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Promotion not found");
        }

        repository.deleteById(id);
    }

    public boolean isCouponValid(
            Promotion promotion, double orderAmount) {

        if (promotion == null
                || promotion.getStartDate() == null
                || promotion.getExpiryDate() == null
                || promotion.getCouponCode() == null
                || orderAmount < 0) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return promotion.isActive()
                && !promotion.getCouponCode().isBlank()
                && promotion.getDiscountPercentage() > 0
                && promotion.getDiscountPercentage() <= 100
                && promotion.getMinimumOrderAmount() >= 0
                && orderAmount >= promotion.getMinimumOrderAmount()
                && !today.isBefore(promotion.getStartDate())
                && !today.isAfter(promotion.getExpiryDate());
    }

    public double calculateDiscount(
            Promotion promotion, double orderAmount) {

        if (!isCouponValid(promotion, orderAmount)) {
            return 0.0;
        }

        return orderAmount
                * promotion.getDiscountPercentage() / 100.0;
    }

    private void validatePromotion(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException(
                    "Promotion is required");
        }

        if (promotion.getName() == null
                || promotion.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Promotion name is required");
        }

        if (promotion.getCouponCode() == null
                || promotion.getCouponCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Coupon code is required");
        }

        if (promotion.getDiscountPercentage() <= 0
                || promotion.getDiscountPercentage() > 100) {
            throw new IllegalArgumentException(
                    "Discount must be between 0 and 100");
        }

        if (promotion.getMinimumOrderAmount() < 0) {
            throw new IllegalArgumentException(
                    "Minimum order amount cannot be negative");
        }

        if (promotion.getStartDate() == null
                || promotion.getExpiryDate() == null
                || promotion.getExpiryDate().isBefore(
                promotion.getStartDate())) {
            throw new IllegalArgumentException(
                    "Invalid promotion dates");
        }
    }
}
