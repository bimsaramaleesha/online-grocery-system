package com.grocery.inventory.controller;

import com.grocery.common.FileHandler;
import com.grocery.inventory.dto.ItemForm;
import com.grocery.inventory.dto.ItemForm.ProductView;
import com.grocery.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles both the Admin Inventory Management (/inventory) and the Customer Storefront (/shop).
 * Self-contained inside the inventory module so other modules can be modified independently.
 */
@Controller
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // ---------- 1. Customer Storefront (/shop) ----------

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String sort, Model model) {
        boolean browsing = (q == null || q.isBlank()) && (category == null || category.isBlank());
        model.addAttribute("products", service.shopProducts(q, category, sort));
        model.addAttribute("categories", service.shopCategories());
        model.addAttribute("deals", browsing ? service.shopDeals() : null);
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        return "inventory/shop";
    }

    @GetMapping("/shop/{id}")
    public String product(@PathVariable String id, Model model, RedirectAttributes redirect) {
        Optional<ProductView> product = service.findShopProduct(id);
        if (product.isEmpty()) {
            redirect.addFlashAttribute("error", "That product is not available.");
            return "redirect:/shop";
        }
        model.addAttribute("p", product.get());
        model.addAttribute("related", service.relatedProducts(product.get()));
        model.addAttribute("reviews", readReviewsForProduct(id));
        return "inventory/product";
    }

    /**
     * Fault-tolerant helper: reads product reviews from data/feedbacks.txt or data/reviews.txt
     * without depending on the feedback Java package. If the file is missing or corrupt, returns an empty list.
     */
    private List<Map<String, String>> readReviewsForProduct(String itemId) {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            for (String line : FileHandler.readLines("feedbacks.txt")) {
                String[] p = line.split("\\|", -1);
                // REVIEW|id|itemId|userId|userName|rating|title|comment|createdDate
                if (p.length >= 9 && "REVIEW".equalsIgnoreCase(p[0]) && itemId.equalsIgnoreCase(p[2])) {
                    int rating = Math.max(1, Math.min(5, Integer.parseInt(p[5])));
                    result.add(Map.of(
                            "title", p[6],
                            "stars", "⭐".repeat(rating),
                            "comment", p[7],
                            "userName", p[4],
                            "createdDate", p[8]
                    ));
                }
            }
        } catch (Exception ignored) {
            // Fault isolation: if feedback data is unavailable, product page still loads cleanly
        }
        return result;
    }

    // ---------- 2. Admin Inventory CRUD (/inventory) ----------

    @GetMapping("/inventory")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String filter, Model model) {
        model.addAttribute("items", service.search(q, filter));
        model.addAttribute("summary", service.summary());
        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        return "inventory/list";
    }

    @GetMapping("/inventory/new")
    public String showAddForm(Model model) {
        model.addAttribute("form", new ItemForm());
        model.addAttribute("editMode", false);
        return "inventory/form";
    }

    @PostMapping("/inventory/new")
    public String add(@Valid @ModelAttribute("form") ItemForm form, BindingResult binding,
                      Model model, RedirectAttributes redirect) {
        model.addAttribute("editMode", false);
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please check the number and date fields.");
            return "inventory/form";
        }
        try {
            service.addItem(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "inventory/form";
        }
        redirect.addFlashAttribute("message", "Item added successfully.");
        return "redirect:/inventory";
    }

    @GetMapping("/inventory/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", ItemForm.from(service.getById(id)));
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventory";
        }
        model.addAttribute("editMode", true);
        return "inventory/form";
    }

    @PostMapping("/inventory/{id}/edit")
    public String update(@PathVariable String id,
                         @Valid @ModelAttribute("form") ItemForm form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        form.setId(id);
        model.addAttribute("editMode", true);
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please check the number and date fields.");
            return "inventory/form";
        }
        try {
            service.updateItem(id, form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "inventory/form";
        }
        redirect.addFlashAttribute("message", "Item updated successfully.");
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirect) {
        try {
            service.deleteItem(id);
            redirect.addFlashAttribute("message", "Item deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory";
    }
}
