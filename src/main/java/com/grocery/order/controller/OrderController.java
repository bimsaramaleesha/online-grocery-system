package com.grocery.order.controller;

import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.service.CartService;
import com.grocery.order.service.OrderService;
import com.grocery.user.model.Customer;
import com.grocery.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller handling checkout, order placement, tracking, and cancellation.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    public OrderController(OrderService orderService, CartService cartService, UserService userService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
    }

    private String resolveUserId(HttpSession session) {
        String sessionUser = (String) session.getAttribute("userId");
        return (sessionUser != null && !sessionUser.isBlank()) ? sessionUser : "U001";
    }

    /**
     * Lists all orders placed by the current user.
     */
    @GetMapping
    public String listOrders(Model model, HttpSession session) {
        String userId = resolveUserId(session);
        List<Order> orders = orderService.getOrdersForUser(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    /**
     * Renders the checkout page with cart contents and delivery details form.
     */
    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = cartService.getCartForUser(userId);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add grocery items first.");
            return "redirect:/cart";
        }

        double subtotal = cartService.calculateSubtotal(cartItems);

        if (!model.containsAttribute("checkoutForm")) {
            CheckoutForm form = new CheckoutForm();
            Object userName = session.getAttribute("userName");
            if (userName != null && !userName.toString().isBlank()) {
                form.setFullName(userName.toString());
            }
            userService.findById(userId).ifPresent(u -> {
                form.setFullName(u.getName());
                if (u instanceof Customer c) {
                    form.setContactPhone(c.getPhone());
                    form.setShippingAddress(c.getAddress());
                }
            });
            model.addAttribute("checkoutForm", form);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        return "order/checkout";
    }

    /**
     * Processes order submission from the checkout form.
     */
    @PostMapping("/checkout")
    public String processCheckout(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userId = resolveUserId(session);
        List<CartItem> cartItems = cartService.getCartForUser(userId);

        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add grocery items first.");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", cartService.calculateSubtotal(cartItems));
            return "order/checkout";
        }

        try {
            Order placedOrder = orderService.placeOrder(userId, form);
            return "redirect:/orders/" + placedOrder.getOrderId() + "/success";
        } catch (Exception e) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", cartService.calculateSubtotal(cartItems));
            model.addAttribute("errorMessage", e.getMessage());
            return "order/checkout";
        }
    }

    /**
     * Displays confirmation screen immediately after order placement.
     */
    @GetMapping("/{orderId}/success")
    public String orderSuccess(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found: " + orderId);
            return "redirect:/orders";
        }

        model.addAttribute("order", orderOpt.get());
        return "order/order-success";
    }

    /**
     * Displays detailed tracking view and receipt for a single order.
     */
    @GetMapping("/{orderId}")
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

    /**
     * Cancels an order if it is in PENDING state.
     */
    @PostMapping("/{orderId}/cancel")
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

    /**
     * Updates an order status (used for demonstration and order flow verification).
     */
    @PostMapping("/{orderId}/status")
    public String updateStatus(@PathVariable String orderId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        OrderStatus newStatus = OrderStatus.fromString(status);
        orderService.updateOrderStatus(orderId, newStatus);
        redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus.getDisplayName());
        return "redirect:/orders/" + orderId;
    }
}
