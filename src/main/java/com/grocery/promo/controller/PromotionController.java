
package com.grocery.promo.controller;

import com.grocery.promo.model.Promotion;
import com.grocery.promo.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Handles API requests for promotions
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {

        this.service = service;
    }

    @GetMapping
    public List<Promotion> getAllPromotions() {
        return service.getAllPromotions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotionById(
            @PathVariable Long id) {

        return service.getPromotionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/coupon/{code}")
    public ResponseEntity<Promotion> getByCouponCode(
            @PathVariable String code) {

        return service.getByCouponCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Promotion> createPromotion(
            @RequestBody Promotion promotion) {

        return ResponseEntity.ok(
                service.createPromotion(promotion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> updatePromotion(
            @PathVariable Long id,
            @RequestBody Promotion promotion) {

        return ResponseEntity.ok(
                service.updatePromotion(id, promotion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(
            @PathVariable Long id) {

        service.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
