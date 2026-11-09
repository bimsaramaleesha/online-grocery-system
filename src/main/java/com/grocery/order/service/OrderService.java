package com.grocery.order.service;

import com.grocery.common.FileHandler;
import com.grocery.order.model.CartItem;
import com.grocery.order.model.CheckoutForm;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderItem;
import com.grocery.order.model.OrderStatus;
import com.grocery.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Member 4 — Unified service for Cart & Order Processing.
 * Reads product catalog (items.txt) and promotional coupons (promotions.txt) fault-tolerantly
 * via FileHandler so the Order module remains completely independent.
 */
@Service
public class OrderService {

    public record CatalogItem(String id, String name, String category, double price, String unit) { }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ---------- 1. Shopping Cart Operations ----------

    public List<CartItem> getCartForUser(String userId) {
        List<CartItem> items = orderRepository.findCartByUserId(userId);
        Map<String, CatalogItem> catalog = loadCatalogMap();
        for (CartItem item : items) {
            CatalogItem cat = catalog.getOrDefault(item.getItemId().toUpperCase(),
                    new CatalogItem(item.getItemId(), "Grocery Item (" + item.getItemId() + ")", "General", 150.0, "unit"));
            item.setItemName(cat.name());
            item.setUnitPrice(cat.price());
            item.setUnit(cat.unit());
        }
        return items;
    }

    public synchronized CartItem addToCart(String userId, String itemId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }
        Optional<CartItem> existingOpt = orderRepository.findCartByUserAndItem(userId, itemId);
        String now = LocalDateTime.now().format(DATE_FORMATTER);

        CartItem saved;
        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(Math.min(99, existing.getQuantity() + quantity));
            existing.setAddedDate(now);
            saved = orderRepository.saveCartItem(existing);
        } else {
            CartItem created = new CartItem(null, userId, itemId, Math.min(99, quantity), now);
            saved = orderRepository.saveCartItem(created);
        }
        CatalogItem cat = loadCatalogMap().getOrDefault(itemId.toUpperCase(),
                new CatalogItem(itemId, "Grocery Item (" + itemId + ")", "General", 150.0, "unit"));
        saved.setItemName(cat.name());
        saved.setUnitPrice(cat.price());
        saved.setUnit(cat.unit());
        return saved;
    }

    public synchronized void updateQuantity(String cartId, int quantity) {
        if (quantity <= 0) {
            orderRepository.deleteCartById(cartId);
            return;
        }
        orderRepository.findCartById(cartId).ifPresent(item -> {
            item.setQuantity(Math.min(99, quantity));
            orderRepository.saveCartItem(item);
        });
    }

    public synchronized void removeFromCart(String cartId) {
        orderRepository.deleteCartById(cartId);
    }

    public synchronized void clearCart(String userId) {
        orderRepository.deleteCartByUserId(userId);
    }

    public double calculateSubtotal(List<CartItem> items) {
        double sum = items.stream().mapToDouble(CartItem::getLineTotal).sum();
        return Math.round(sum * 100.0) / 100.0;
    }

    public int getTotalItemCount(List<CartItem> items) {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public List<CatalogItem> getAvailableCatalog() {
        return new ArrayList<>(loadCatalogMap().values());
    }

    // ---------- 2. Order Checkout & Lifecycle Operations ----------

    public synchronized Order placeOrder(String userId, CheckoutForm form) {
        List<CartItem> cartItems = getCartForUser(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Please add items before checkout.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem(ci.getItemId(), ci.getItemName(), ci.getQuantity(), ci.getUnitPrice());
            orderItems.add(oi);
            subtotal += oi.getLineTotal();
        }
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        String promo = form.getPromoCode() != null ? form.getPromoCode().trim().toUpperCase() : "";
        double discount = calculatePromoDiscount(promo, subtotal);
        double netAmount = Math.max(0.0, Math.round((subtotal - discount) * 100.0) / 100.0);

        String now = LocalDateTime.now().format(DATE_FORMATTER);
        String fullAddress = form.getShippingAddress();
        if (form.getDeliveryNotes() != null && !form.getDeliveryNotes().isBlank()) {
            fullAddress += " (Notes: " + form.getDeliveryNotes().trim() + ")";
        }

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
                .paymentMethod(form.getPaymentMethod())
                .promoCode(promo)
                .build();

        Order saved = orderRepository.save(order);
        clearCart(userId);
        return saved;
    }

    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersForUser(String userId) {
        List<Order> list = new ArrayList<>(orderRepository.findByUserId(userId));
        Collections.reverse(list);
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>(orderRepository.findAll());
        Collections.reverse(list);
        return list;
    }

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

    public boolean updateOrderStatus(String orderId, OrderStatus status) {
        return orderRepository.updateStatus(orderId, status);
    }

    public void deleteOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    // ---------- 3. Fault-Tolerant Promo & Catalog Readers ----------

    public double calculatePromoDiscount(String promoCode, double subtotal) {
        if (promoCode == null || promoCode.isBlank() || subtotal <= 0) {
            return 0.0;
        }
        String code = promoCode.trim().toUpperCase();

        // 1. Check promotions.txt (and promos.txt fallback) fault-tolerantly
        List<String> lines = new ArrayList<>();
        try {
            lines.addAll(FileHandler.readLines("promotions.txt"));
            lines.addAll(FileHandler.readLines("promos.txt"));
        } catch (Exception ignored) {
        }

        for (String line : lines) {
            try {
                String[] p = line.split("\\|", -1);
                // Format A (OOP Promotion): TYPE|id|name|description|couponCode|discountValue|minOrder|start|expiry|active
                if (p.length >= 10 && ("PERCENTAGE".equalsIgnoreCase(p[0]) || "FIXED".equalsIgnoreCase(p[0]))) {
                    String fileCode = p[4].trim().toUpperCase();
                    boolean active = Boolean.parseBoolean(p[9].trim());
                    LocalDate expiry = LocalDate.parse(p[8].trim());
                    if (fileCode.equals(code) && active && !LocalDate.now().isAfter(expiry)) {
                        double val = Double.parseDouble(p[5].trim());
                        double minOrder = Double.parseDouble(p[6].trim());
                        if (subtotal < minOrder) {
                            return 0.0;
                        }
                        double raw = "PERCENTAGE".equalsIgnoreCase(p[0]) ? subtotal * (val / 100.0) : val;
                        return Math.min(subtotal, Math.round(raw * 100.0) / 100.0);
                    }
                }
                // Format B (legacy promos.txt): id|name|description|couponCode|discountPct|start|expiry|active
                else if (p.length >= 8 && p[3].trim().equalsIgnoreCase(code)) {
                    boolean active = Boolean.parseBoolean(p[7].trim());
                    LocalDate expiry = LocalDate.parse(p[6].trim());
                    if (active && !LocalDate.now().isAfter(expiry)) {
                        double pct = Double.parseDouble(p[4].trim());
                        return Math.min(subtotal, Math.round(subtotal * (pct / 100.0) * 100.0) / 100.0);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Built-in fallback coupons so checkout works even if promotions.txt is missing
        return switch (code) {
            case "WELCOME10" -> Math.round(subtotal * 0.10 * 100.0) / 100.0;
            case "FRESH15" -> Math.round(subtotal * 0.15 * 100.0) / 100.0;
            case "SAVE200" -> subtotal >= 1000.0 ? 200.0 : 0.0;
            default -> 0.0;
        };
    }

    private Map<String, CatalogItem> loadCatalogMap() {
        Map<String, CatalogItem> map = new LinkedHashMap<>();
        // Built-in fallback items if items.txt is empty or missing
        map.put("ITM001", new CatalogItem("ITM001", "Organic Red Apples", "Fruits", 650.00, "1 kg"));
        map.put("ITM002", new CatalogItem("ITM002", "Fresh Farm Milk", "Dairy", 420.00, "1 L"));
        map.put("ITM003", new CatalogItem("ITM003", "Upcountry Carrots", "Vegetables", 320.00, "500 g"));
        map.put("ITM004", new CatalogItem("ITM004", "Artisan Wholemeal Bread", "Bakery", 280.00, "450 g loaf"));
        map.put("ITM005", new CatalogItem("ITM005", "Red Samba Rice", "Grains", 1150.00, "5 kg bag"));
        map.put("ITM006", new CatalogItem("ITM006", "Pure Ceylon Cinnamon Tea", "Beverages", 780.00, "200 g pack"));

        try {
            List<String> lines = FileHandler.readLines("items.txt");
            if (!lines.isEmpty()) {
                Map<String, CatalogItem> fileMap = new LinkedHashMap<>();
                for (String line : lines) {
                    String[] p = line.split("\\|", -1);
                    if (p.length >= 6) {
                        String id = p[1].trim().toUpperCase();
                        String name = p[2].trim();
                        double price = Double.parseDouble(p[3].trim());
                        String category = p[5].trim();
                        String unit = p.length >= 9 ? p[8].trim() : "unit";
                        fileMap.put(id, new CatalogItem(id, name, category, price, unit));
                    }
                }
                if (!fileMap.isEmpty()) {
                    return fileMap;
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }
}
