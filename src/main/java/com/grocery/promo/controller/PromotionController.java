package com.grocery.promo.controller;

import com.grocery.promo.model.Promotion;
import com.grocery.promo.service.PromotionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/promos")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String showPromotions(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String editId,
                                 Model model) {
        model.addAttribute("promotions", promotionService.searchPromotions(q));
        model.addAttribute("q", q);
        if (editId != null && !editId.isBlank()) {
            Promotion existing = promotionService.getPromotionById(editId).orElse(new Promotion());
            model.addAttribute("promotion", existing);
            model.addAttribute("editMode", existing.getId() != null);
        } else {
            model.addAttribute("promotion", new Promotion());
            model.addAttribute("editMode", false);
        }
        return "promo/promotions";
    }

    @PostMapping("/add")
    public String addPromotion(@ModelAttribute("promotion") Promotion promotion,
                               RedirectAttributes redirectAttributes) {
        try {
            promotionService.createPromotion(promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/promos";
    }

    @PostMapping("/update/{id}")
    public String updatePromotion(@PathVariable String id,
                                  @ModelAttribute("promotion") Promotion promotion,
                                  RedirectAttributes redirectAttributes) {
        try {
            promotionService.updatePromotion(id, promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/promos";
    }

    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable String id,
                                  RedirectAttributes redirectAttributes) {
        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/promos";
    }
}
