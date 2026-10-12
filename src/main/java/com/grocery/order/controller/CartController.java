package com.grocery.order.controller;

import com.grocery.order.model.CartItem;
import com.grocery.order.service.CartService;
import com.grocery.order.service.ItemCatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller handling user interactions with the shopping cart.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ItemCatalogService itemCatalogService;

    public CartController(CartService cartService, ItemCatalogService itemCatalogService) {
        this.cartService = cartService;
        this.itemCatalogService = itemCatalogService;
    }

    /**
     * Resolves the active user ID from HTTP session, defaulting to U001.
     */
    private String resolveUserId(HttpSession session) {
        String sessionUser = (String) session.getAttribute("userId");
        return (sessionUser != null && !sessionUser.isBlank()) ? sessionUser : "U001";
    }

    /**
     * Displays the current shopping cart and available products to add.
     */
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = cartService.getCartForUser(userId);
        double subtotal = cartService.calculateSubtotal(cartItems);

        model.addAttribute("userId", userId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("itemCount", cartService.getCartItemCount(userId));
        model.addAttribute("availableItems", itemCatalogService.getAvailableCatalog());

        return "order/cart";
    }

    /**
     * Adds an item to the shopping cart.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam String itemId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        cartService.addToCart(userId, itemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to your cart!");
        return "redirect:/cart";
    }

    /**
     * Updates an existing cart item's quantity.
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam String cartId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(cartId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Cart quantity updated.");
        return "redirect:/cart";
    }

    /**
     * Removes an item from the cart.
     */
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam String cartId,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(cartId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    /**
     * Clears all items in the user's cart.
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        cartService.clearCart(userId);
        redirectAttributes.addFlashAttribute("successMessage", "Your cart has been emptied.");
        return "redirect:/cart";
    }
}
