package com.grocery.promo.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.promo.model.Promotion;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves promotions using the shared FileHandler.
 * Each promotion is stored as one line in data/promos.txt.
 */
@Repository
public class PromotionRepository {

    private static final String FILE_NAME = "promos.txt";

    /**
     * Returns all promotions stored in the text file.
     */
    public List<Promotion> findAll() {
        List<Promotion> promotions = new ArrayList<>();

        for (String line : FileHandler.readLines(FILE_NAME)) {
            Promotion promotion = fromLine(line);
            if (promotion != null) {
                promotions.add(promotion);
            }
        }

        return promotions;
    }

    /**
     * Finds a promotion by its ID.
     */
    public Optional<Promotion> findById(String id) {
        return findAll().stream()
                .filter(promotion -> promotion.getId().equals(id))
                .findFirst();
    }

    /**
     * Finds a promotion by coupon code, ignoring letter case.
     */
    public Optional<Promotion> findByCouponCodeIgnoreCase(String couponCode) {
        if (couponCode == null) {
            return Optional.empty();
        }

        return findAll().stream()
                .filter(promotion ->
                        promotion.getCouponCode().equalsIgnoreCase(couponCode.trim()))
                .findFirst();
    }

    /**
     * Returns promotions marked as active.
     */
    public List<Promotion> findByActiveTrue() {
        return findAll().stream()
                .filter(Promotion::isActive)
                .toList();
    }

    /**
     * Saves a new promotion or updates an existing promotion.
     */
    public Promotion save(Promotion promotion) {
        List<Promotion> promotions = findAll();

        if (promotion.getId() == null || promotion.getId().isBlank()) {
            List<String> existingIds = promotions.stream()
                    .map(Promotion::getId)
                    .toList();

            promotion.setId(IdGenerator.nextId("PRM", existingIds));
            promotions.add(promotion);
        } else {
            boolean updated = false;

            for (int i = 0; i < promotions.size(); i++) {
                if (promotions.get(i).getId().equals(promotion.getId())) {
                    promotions.set(i, promotion);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                promotions.add(promotion);
            }
        }

        writeAll(promotions);
        return promotion;
    }

    /**
     * Checks whether a promotion ID exists.
     */
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /**
     * Deletes a promotion using its ID.
     */
    public void deleteById(String id) {
        List<Promotion> promotions = findAll();
        promotions.removeIf(promotion -> promotion.getId().equals(id));
        writeAll(promotions);
    }

    /**
     * Writes all promotions using the shared FileHandler.
     */
    private void writeAll(List<Promotion> promotions) {
        List<String> lines = new ArrayList<>();

        for (Promotion promotion : promotions) {
            lines.add(toLine(promotion));
        }

        FileHandler.writeLines(FILE_NAME, lines);
    }

    /**
     * Converts a promotion into a pipe-separated record.
     */
    private String toLine(Promotion promotion) {
        return String.join("|",
                FileHandler.clean(promotion.getId()),
                FileHandler.clean(promotion.getName()),
                FileHandler.clean(promotion.getDescription()),
                FileHandler.clean(promotion.getCouponCode()),
                String.valueOf(promotion.getDiscountPercentage()),
                String.valueOf(promotion.getMinimumOrderAmount()),
                promotion.getStartDate() == null
                        ? "" : promotion.getStartDate().toString(),
                promotion.getExpiryDate() == null
                        ? "" : promotion.getExpiryDate().toString(),
                String.valueOf(promotion.isActive())
        );
    }

    /**
     * Converts a pipe-separated record into a Promotion object.
     */
    private Promotion fromLine(String line) {
        try {
            String[] fields = line.split("\\|", -1);

            if (fields.length != 9) {
                return null;
            }

            Promotion promotion = new Promotion();
            promotion.setId(fields[0]);
            promotion.setName(fields[1]);
            promotion.setDescription(fields[2]);
            promotion.setCouponCode(fields[3]);
            promotion.setDiscountPercentage(Double.parseDouble(fields[4]));
            promotion.setMinimumOrderAmount(Double.parseDouble(fields[5]));
            promotion.setStartDate(
                    fields[6].isBlank() ? null : LocalDate.parse(fields[6]));
            promotion.setExpiryDate(
                    fields[7].isBlank() ? null : LocalDate.parse(fields[7]));
            promotion.setActive(Boolean.parseBoolean(fields[8]));

            return promotion;

        } catch (RuntimeException exception) {
            // Ignore malformed records rather than crashing the entire page.
            return null;
        }
    }
}

