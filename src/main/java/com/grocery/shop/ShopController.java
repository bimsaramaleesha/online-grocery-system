package com.grocery.shop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * The customer storefront (public: guests can look around, they log in only to buy).
 *   GET /shop        all products (?q=search, ?category=Toys, ?sort=price-asc|price-desc|name)
 *   GET /shop/{id}   one product page
 * "Add to cart" posts to the Order module's existing /cart/add.
 */
@Controller
public class ShopController {

    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }

    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String sort, Model model) {
        boolean browsing = isBlank(q) && isBlank(category);       // plain home view = show hero + deals
        model.addAttribute("products", service.products(q, category, sort));
        model.addAttribute("categories", service.categories());
        model.addAttribute("deals", browsing ? service.deals() : null);
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        return "shop/home";
    }

    @GetMapping("/shop/{id}")
    public String product(@PathVariable String id, Model model, RedirectAttributes redirect) {
        Optional<ProductView> product = service.find(id);
        if (product.isEmpty()) {
            redirect.addFlashAttribute("error", "That product is not available.");
            return "redirect:/shop";
        }
        model.addAttribute("p", product.get());
        model.addAttribute("related", service.related(product.get()));
        return "shop/product";
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
