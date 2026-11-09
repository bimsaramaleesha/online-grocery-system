package com.grocery.order.repository;

import com.grocery.order.model.CartItem;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Member 4 — Repository abstraction for persisting Orders (orders.txt) and CartItems (carts.txt).
 */
public interface OrderRepository {

    // --- Order Persistence (orders.txt) ---
    List<Order> findAll();
    Optional<Order> findById(String orderId);
    List<Order> findByUserId(String userId);
    Order save(Order order);
    boolean updateStatus(String orderId, OrderStatus status);
    void deleteById(String orderId);

    // --- Cart Persistence (carts.txt) ---
    List<CartItem> findAllCartItems();
    List<CartItem> findCartByUserId(String userId);
    Optional<CartItem> findCartById(String cartId);
    Optional<CartItem> findCartByUserAndItem(String userId, String itemId);
    CartItem saveCartItem(CartItem item);
    void deleteCartById(String cartId);
    void deleteCartByUserId(String userId);
}
