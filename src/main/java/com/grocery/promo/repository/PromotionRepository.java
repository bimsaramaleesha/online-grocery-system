package com.grocery.promo.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.promo.model.Promotion;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Member 5 — Stores and retrieves promotions using the shared FileHandler in data/promotions.txt.
 */
@Repository
public class PromotionRepository {

    private final String fileName;

    public PromotionRepository(@Value("${grocery.data.promotions-file:promotions.txt}") String fileName) {
        this.fileName = fileName;
    }

    @PostConstruct
    public synchronized void seedDefaults() {
        if (!FileHandler.readLines(fileName).isEmpty()) {
            return;
        }
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().plusMonths(6);
        List<Promotion> defaults = List.of(
                new Promotion.PercentagePromotion("PRM001", "Welcome Harvest Offer",
                        "10% discount on any fresh farm basket", "WELCOME10", 10.0, 0.0, start, end, true),
                new Promotion.FixedPromotion("PRM002", "Big Basket Saver",
                        "Flat Rs. 200 off orders of Rs. 1,000 or more", "SAVE200", 200.0, 1000.0, start, end, true)
        );
        writeAll(defaults);
    }

    public List<Promotion> findAll() {
        List<Promotion> promotions = new ArrayList<>();
        for (String line : FileHandler.readLines(fileName)) {
            Promotion promotion = Promotion.fromFileLine(line);
            if (promotion != null) {
                promotions.add(promotion);
            }
        }
        return promotions;
    }

    public Optional<Promotion> findById(String id) {
        return findAll().stream()
                .filter(promotion -> promotion.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public Optional<Promotion> findByCouponCodeIgnoreCase(String couponCode) {
        if (couponCode == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(promotion -> promotion.getCouponCode().equalsIgnoreCase(couponCode.trim()))
                .findFirst();
    }

    public List<Promotion> findByActiveTrue() {
        return findAll().stream()
                .filter(Promotion::isActive)
                .toList();
    }

    public synchronized Promotion save(Promotion promotion) {
        Promotion typed = Promotion.createTyped(promotion);
        List<Promotion> promotions = findAll();

        if (typed.getId() == null || typed.getId().isBlank()) {
            List<String> existingIds = promotions.stream()
                    .map(Promotion::getId)
                    .toList();
            typed.setId(IdGenerator.nextId("PRM", existingIds));
            promotions.add(typed);
        } else {
            boolean updated = false;
            for (int i = 0; i < promotions.size(); i++) {
                if (promotions.get(i).getId().equalsIgnoreCase(typed.getId())) {
                    promotions.set(i, typed);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                promotions.add(typed);
            }
        }

        writeAll(promotions);
        return typed;
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public synchronized void deleteById(String id) {
        List<Promotion> promotions = findAll();
        promotions.removeIf(promotion -> promotion.getId().equalsIgnoreCase(id));
        writeAll(promotions);
    }

    private void writeAll(List<Promotion> promotions) {
        List<String> lines = new ArrayList<>();
        for (Promotion promotion : promotions) {
            lines.add(promotion.toFileLine());
        }
        FileHandler.writeLines(fileName, lines);
    }
}
