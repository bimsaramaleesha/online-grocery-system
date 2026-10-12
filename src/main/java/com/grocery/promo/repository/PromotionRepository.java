
package com.grocery.promo.repository;

import com.grocery.promo.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Handles database operations for promotions
public interface PromotionRepository
        extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCouponCodeIgnoreCase(
            String couponCode);

    List<Promotion> findByActiveTrue();
}
