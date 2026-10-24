package com.grocery.promo.controller;

import com.grocery.promo.model.Promotion;
import com.grocery.promo.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/promos")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String showPromotions(Model model) {
        model.addAttribute("promotions",
                promotionService.getAllPromotions());
        model.addAttribute("promotion", new Promotion());
        return "promo/promotions";
    }

    @PostMapping("/add")
    public String addPromotion(
            @ModelAttribute("promotion") Promotion promotion,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.createPromotion(promotion);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Promotion added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }
        return "redirect:/promos";
    }

    @PostMapping("/delete/{id}")
    public String deletePromotion(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Promotion deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }
        return "redirect:/promos";
    }
}