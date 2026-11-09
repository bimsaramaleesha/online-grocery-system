package com.grocery.order.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.order.model.CartItem;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Member 4 — File-backed repository for Orders (data/orders.txt) and CartItems (data/carts.txt).
 */
@Repository
public class FileOrderRepository implements OrderRepository {

    private final String ordersFile;
    private final String cartsFile;

    public FileOrderRepository(
            @Value("${grocery.data.orders-file:orders.txt}") String ordersFile,
            @Value("${grocery.data.carts-file:carts.txt}") String cartsFile) {
        this.ordersFile = ordersFile;
        this.cartsFile = cartsFile;
    }

    // ---------- 1. Order Persistence (orders.txt) ----------

    @Override
    public List<Order> findAll() {
        List<String> lines = FileHandler.readLines(ordersFile);
        List<Order> orders = new ArrayList<>();
        for (String line : lines) {
            Order order = Order.fromRecordLine(line);
            if (order != null) {
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(o -> orderId.equalsIgnoreCase(o.getOrderId()))
                .findFirst();
    }

    @Override
    public List<Order> findByUserId(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(o -> userId.equalsIgnoreCase(o.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized Order save(Order order) {
        List<Order> all = findAll();
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            List<String> existingIds = all.stream().map(Order::getOrderId).collect(Collectors.toList());
            String newId = IdGenerator.nextId("ORD", existingIds);
            order.setOrderId(newId);
            FileHandler.appendLine(ordersFile, order.toRecordLine());
            return order;
        }

        boolean found = false;
        List<String> updatedLines = new ArrayList<>();
        for (Order o : all) {
            if (o.getOrderId().equalsIgnoreCase(order.getOrderId())) {
                updatedLines.add(order.toRecordLine());
                found = true;
            } else {
                updatedLines.add(o.toRecordLine());
            }
        }

        if (!found) {
            updatedLines.add(order.toRecordLine());
        }
        FileHandler.writeLines(ordersFile, updatedLines);
        return order;
    }

    @Override
    public synchronized boolean updateStatus(String orderId, OrderStatus status) {
        Optional<Order> orderOpt = findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }
        Order order = orderOpt.get();
        order.setStatus(status);
        save(order);
        return true;
    }

    @Override
    public synchronized void deleteById(String orderId) {
        if (orderId == null) {
            return;
        }
        List<String> remaining = findAll().stream()
                .filter(o -> !orderId.equalsIgnoreCase(o.getOrderId()))
                .map(Order::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(ordersFile, remaining);
    }

    // ---------- 2. Cart Persistence (carts.txt) ----------

    @Override
    public List<CartItem> findAllCartItems() {
        List<String> lines = FileHandler.readLines(cartsFile);
        List<CartItem> items = new ArrayList<>();
        for (String line : lines) {
            CartItem item = CartItem.fromRecordLine(line);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public List<CartItem> findCartByUserId(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return findAllCartItems().stream()
                .filter(i -> userId.equalsIgnoreCase(i.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CartItem> findCartById(String cartId) {
        if (cartId == null) {
            return Optional.empty();
        }
        return findAllCartItems().stream()
                .filter(i -> cartId.equalsIgnoreCase(i.getCartId()))
                .findFirst();
    }

    @Override
    public Optional<CartItem> findCartByUserAndItem(String userId, String itemId) {
        if (userId == null || itemId == null) {
            return Optional.empty();
        }
        return findAllCartItems().stream()
                .filter(i -> userId.equalsIgnoreCase(i.getUserId()) && itemId.equalsIgnoreCase(i.getItemId()))
                .findFirst();
    }

    @Override
    public synchronized CartItem saveCartItem(CartItem item) {
        List<CartItem> all = findAllCartItems();
        if (item.getCartId() == null || item.getCartId().isBlank()) {
            List<String> existingIds = all.stream().map(CartItem::getCartId).collect(Collectors.toList());
            item.setCartId(IdGenerator.nextId("CRT", existingIds));
            FileHandler.appendLine(cartsFile, item.toRecordLine());
            return item;
        }

        boolean found = false;
        List<String> updatedLines = new ArrayList<>();
        for (CartItem existing : all) {
            if (existing.getCartId().equalsIgnoreCase(item.getCartId())) {
                updatedLines.add(item.toRecordLine());
                found = true;
            } else {
                updatedLines.add(existing.toRecordLine());
            }
        }
        if (!found) {
            updatedLines.add(item.toRecordLine());
        }
        FileHandler.writeLines(cartsFile, updatedLines);
        return item;
    }

    @Override
    public synchronized void deleteCartById(String cartId) {
        if (cartId == null) {
            return;
        }
        List<String> remaining = findAllCartItems().stream()
                .filter(i -> !cartId.equalsIgnoreCase(i.getCartId()))
                .map(CartItem::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(cartsFile, remaining);
    }

    @Override
    public synchronized void deleteCartByUserId(String userId) {
        if (userId == null) {
            return;
        }
        List<String> remaining = findAllCartItems().stream()
                .filter(i -> !userId.equalsIgnoreCase(i.getUserId()))
                .map(CartItem::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(cartsFile, remaining);
    }
}
