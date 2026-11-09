package com.grocery.order.controller;

import com.grocery.common.FileHandler;
import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
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

import java.util.List;
import java.util.Optional;

/**
 * Member 4 — Unified Controller for Cart (/cart) and Order Processing (/orders).
 * Completely independent of other modules' Java classes.
 */
@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private String resolveUserId(HttpSession session) {
        String sessionUser = (String) session.getAttribute("userId");
        return (sessionUser != null && !sessionUser.isBlank()) ? sessionUser : "U001";
    }

    // ---------- 1. Shopping Cart Routes (/cart) ----------

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = orderService.getCartForUser(userId);
        double subtotal = orderService.calculateSubtotal(cartItems);
        int itemCount = orderService.getTotalItemCount(cartItems);

        model.addAttribute("userId", userId);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("availableItems", orderService.getAvailableCatalog());
        return "order/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String itemId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        CartItem added = orderService.addToCart(userId, itemId, quantity);
        redirectAttributes.addFlashAttribute("successMessage",
                "Added " + quantity + " x " + added.getItemName() + " to your shopping cart!");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam String cartId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        orderService.updateQuantity(cartId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Cart quantity updated.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam String cartId,
                             RedirectAttributes redirectAttributes) {
        orderService.removeFromCart(cartId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        orderService.clearCart(userId);
        redirectAttributes.addFlashAttribute("successMessage", "Your shopping cart has been cleared.");
        return "redirect:/cart";
    }

    // ---------- 2. Order Checkout & Tracking Routes (/orders) ----------

    @GetMapping("/orders")
    public String listOrders(Model model, HttpSession session) {
        String userId = resolveUserId(session);
        List<Order> orders = orderService.getOrdersForUser(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @GetMapping("/orders/checkout")
    public String checkoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = orderService.getCartForUser(userId);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add grocery items first.");
            return "redirect:/cart";
        }

        double subtotal = orderService.calculateSubtotal(cartItems);

        if (!model.containsAttribute("checkoutForm")) {
            CheckoutForm form = new CheckoutForm();
            Object userName = session.getAttribute("userName");
            if (userName != null && !userName.toString().isBlank()) {
                form.setFullName(userName.toString());
            }
            prefillUserFromDataFile(userId, form);
            model.addAttribute("checkoutForm", form);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        return "order/checkout";
    }

    @PostMapping("/orders/checkout")
    public String processCheckout(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = orderService.getCartForUser(userId);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add grocery items first.");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", orderService.calculateSubtotal(cartItems));
            return "order/checkout";
        }

        try {
            Order placedOrder = orderService.placeOrder(userId, form);
            return "redirect:/orders/" + placedOrder.getOrderId() + "/success";
        } catch (Exception e) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", orderService.calculateSubtotal(cartItems));
            model.addAttribute("errorMessage", e.getMessage());
            return "order/checkout";
        }
    }

    @GetMapping("/orders/{orderId}/success")
    public String orderSuccess(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found: " + orderId);
            return "redirect:/orders";
        }

        model.addAttribute("order", orderOpt.get());
        return "order/order-success";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found: " + orderId);
            return "redirect:/orders";
        }

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("allStatuses", OrderStatus.values());
        return "order/order-details";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable String orderId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = orderService.cancelOrder(orderId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Order " + orderId + " has been successfully cancelled.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Could not find order " + orderId);
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateStatus(@PathVariable String orderId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        OrderStatus newStatus = OrderStatus.fromString(status);
        orderService.updateOrderStatus(orderId, newStatus);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getDisplayName());
        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/orders/{orderId}/delete")
    public String deleteOrder(@PathVariable String orderId, RedirectAttributes redirectAttributes) {
        orderService.deleteOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Order " + orderId + " deleted.");
        return "redirect:/orders";
    }

    /** Fault-tolerantly reads customer name, phone, and address from users.txt. */
    private void prefillUserFromDataFile(String userId, CheckoutForm form) {
        try {
            for (String line : FileHandler.readLines("users.txt")) {
                String[] p = line.split("\\|", -1);
                // CUSTOMER|id|name|email|hash|createdDate|phone|address
                if (p.length >= 6 && p[1].trim().equalsIgnoreCase(userId)) {
                    form.setFullName(p[2].trim());
                    if ("CUSTOMER".equalsIgnoreCase(p[0].trim()) && p.length >= 8) {
                        form.setContactPhone(p[6].trim());
                        form.setShippingAddress(p[7].trim());
                    }
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
