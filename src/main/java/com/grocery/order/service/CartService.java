package com.grocery.order.service;

import com.grocery.order.model.CartItem;
import com.grocery.order.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Encapsulates business logic for managing user shopping carts.
 */
@Service
public class CartService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CartRepository cartRepository;
    private final ItemCatalogService itemCatalogService;

    public CartService(CartRepository cartRepository, ItemCatalogService itemCatalogService) {
        this.cartRepository = cartRepository;
        this.itemCatalogService = itemCatalogService;
    }

    /**
     * Retrieves all items in the user's cart, enriched with catalog product info (name, price, unit).
     */
    public List<CartItem> getCartForUser(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        for (CartItem item : items) {
            enrichCartItem(item);
        }
        return items;
    }

    /**
     * Adds an item to the user's cart. If the item already exists in the cart,
     * its quantity is incremented.
     */
    public CartItem addToCart(String userId, String itemId, int quantity) {
        if (quantity < 1) {
            quantity = 1;
        }

        Optional<CartItem> existingOpt = cartRepository.findByUserAndItem(userId, itemId);
        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + quantity);
            cartRepository.save(existing);
            enrichCartItem(existing);
            return existing;
        }

        String now = LocalDateTime.now().format(DATE_FORMATTER);
        CartItem newItem = new CartItem(null, userId, itemId, quantity, now);
        cartRepository.save(newItem);
        enrichCartItem(newItem);
        return newItem;
    }

    /** Updates the quantity of a cart item. If quantity <= 0, the item is removed. */
    public void updateQuantity(String cartId, int quantity) {
        if (quantity <= 0) {
            cartRepository.deleteById(cartId);
            return;
        }
        Optional<CartItem> itemOpt = cartRepository.findById(cartId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(quantity);
            cartRepository.save(item);
        }
    }

    /** Removes a specific item from the cart */
    public void removeFromCart(String cartId) {
        cartRepository.deleteById(cartId);
    }

    /** Clears the entire cart for a user */
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }

    /** Calculates subtotal of all items in the cart */
    public double calculateSubtotal(List<CartItem> items) {
        double subtotal = 0.0;
        for (CartItem item : items) {
            enrichCartItem(item);
            subtotal += item.getLineTotal();
        }
        return Math.round(subtotal * 100.0) / 100.0;
    }

    /** Total number of item units in user's cart */
    public int getCartItemCount(String userId) {
        return cartRepository.findByUserId(userId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private void enrichCartItem(CartItem item) {
        itemCatalogService.findItemById(item.getItemId()).ifPresentOrElse(cat -> {
            item.setItemName(cat.getName());
            item.setUnitPrice(cat.getPrice());
            item.setUnit(cat.getUnit());
        }, () -> {
            if (item.getItemName() == null || item.getItemName().equals(item.getItemId())) {
                item.setItemName("Grocery Item (" + item.getItemId() + ")");
                item.setUnitPrice(100.00);
            }
        });
    }
}
