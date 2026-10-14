package com.grocery.order.service;

import com.grocery.common.FileHandler;
import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderItem;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.repository.OrderRepository;
import com.grocery.order.strategy.discount.DiscountStrategy;
import com.grocery.order.strategy.discount.DiscountStrategyFactory;
import com.grocery.order.strategy.payment.PaymentStrategy;
import com.grocery.order.strategy.payment.PaymentStrategyFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Encapsulates business logic for placing, managing, and tracking customer orders.
 * Applies Strategy Pattern for pricing/payment and Builder Pattern for order construction.
 */
@Service
public class OrderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final PaymentStrategyFactory paymentStrategyFactory;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        DiscountStrategyFactory discountStrategyFactory,
                        PaymentStrategyFactory paymentStrategyFactory) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.discountStrategyFactory = discountStrategyFactory;
        this.paymentStrategyFactory = paymentStrategyFactory;
    }

    /**
     * Converts the user's active cart items into a confirmed Order,
     * applies promotional discounts via Strategy Pattern, constructs Order
     * using Builder Pattern, records the order in orders.txt, and clears the cart.
     */
    public synchronized Order placeOrder(String userId, CheckoutForm form) {
        List<CartItem> cartItems = cartService.getCartForUser(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Please add items before checkout.");
        }

        // Snapshot cart items into OrderItem records with locked prices
        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem(ci.getItemId(), ci.getItemName(), ci.getQuantity(), ci.getUnitPrice());
            orderItems.add(oi);
            subtotal += oi.getLineTotal();
        }
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        // 1. Strategy Pattern: Calculate promotional discount
        String promo = form.getPromoCode() != null ? form.getPromoCode().trim().toUpperCase() : "";
        DiscountStrategy discountStrategy = discountStrategyFactory.getStrategy(promo);
        double discount = discountStrategy.calculateDiscount(subtotal);
        double netAmount = Math.max(0.0, Math.round((subtotal - discount) * 100.0) / 100.0);

        // 2. Strategy Pattern: Resolve payment handling instructions
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(form.getPaymentMethod());

        String now = LocalDateTime.now().format(DATE_FORMATTER);

        String fullAddress = form.getShippingAddress();
        if (form.getDeliveryNotes() != null && !form.getDeliveryNotes().isBlank()) {
            fullAddress += " (Notes: " + form.getDeliveryNotes().trim() + ")";
        }

        // 3. Builder Pattern: Cleanly construct the Order object
        Order order = Order.builder()
                .userId(userId)
                .items(orderItems)
                .subtotal(subtotal)
                .discountAmount(discount)
                .netAmount(netAmount)
                .status(OrderStatus.PENDING)
                .orderDate(now)
                .shippingAddress(fullAddress)
                .contactPhone(form.getContactPhone())
                .paymentMethod(paymentStrategy.getMethodName())
                .promoCode(promo)
                .build();

        Order saved = orderRepository.save(order);

        // Clear cart now that order is placed
        cartService.clearCart(userId);

        return saved;
    }

    /** Retrieves an order by its unique ID */
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    /** Retrieves all orders placed by a specific user, sorted newest first */
    public List<Order> getOrdersForUser(String userId) {
        List<Order> list = new ArrayList<>(orderRepository.findByUserId(userId));
        Collections.reverse(list);
        return list;
    }

    /** Retrieves all orders in the system, sorted newest first */
    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>(orderRepository.findAll());
        Collections.reverse(list);
        return list;
    }

    /**
     * Cancels an order if it is still in PENDING state.
     */
    public boolean cancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }
        Order order = orderOpt.get();
        if (!order.canBeCancelled()) {
            throw new IllegalStateException("Only pending orders can be cancelled. Current status: " + order.getStatus().getDisplayName());
        }
        return orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);
    }

    /** Updates the status of an existing order */
    public boolean updateOrderStatus(String orderId, OrderStatus status) {
        return orderRepository.updateStatus(orderId, status);
    }

    /**
     * Evaluates promo discount using Strategy Pattern via DiscountStrategyFactory.
     */
    public double calculatePromoDiscount(String promoCode, double subtotal) {
        return discountStrategyFactory.getStrategy(promoCode).calculateDiscount(subtotal);
    }
}
