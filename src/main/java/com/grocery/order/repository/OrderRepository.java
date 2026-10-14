package com.grocery.order.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.order.model.Order;
import com.grocery.order.model.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Accesses and persists Order records in data/orders.txt using FileHandler.
 */
@Repository
public class OrderRepository {

    private static final String FILE_NAME = "orders.txt";

    /** Reads all orders from orders.txt */
    public List<Order> findAll() {
        List<String> lines = FileHandler.readLines(FILE_NAME);
        List<Order> orders = new ArrayList<>();
        for (String line : lines) {
            Order order = Order.fromRecordLine(line);
            if (order != null) {
                orders.add(order);
            }
        }
        return orders;
    }

    /** Finds an order by its unique orderId */
    public Optional<Order> findById(String orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(o -> orderId.equalsIgnoreCase(o.getOrderId()))
                .findFirst();
    }

    /** Finds all orders placed by a specific user, sorted newest first */
    public List<Order> findByUserId(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(o -> userId.equalsIgnoreCase(o.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Persists an order. Assigns next ORD prefix ID if not present.
     */
    public synchronized Order save(Order order) {
        List<Order> all = findAll();
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            List<String> existingIds = all.stream().map(Order::getOrderId).collect(Collectors.toList());
            String newId = IdGenerator.nextId("ORD", existingIds);
            order.setOrderId(newId);
            FileHandler.appendLine(FILE_NAME, order.toRecordLine());
            return order;
        }

        // Update existing order
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
        FileHandler.writeLines(FILE_NAME, updatedLines);
        return order;
    }

    /** Updates the status of an order */
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

    /** Deletes an order by orderId */
    public synchronized void deleteById(String orderId) {
        if (orderId == null) return;
        List<Order> all = findAll();
        List<String> remaining = all.stream()
                .filter(o -> !orderId.equalsIgnoreCase(o.getOrderId()))
                .map(Order::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(FILE_NAME, remaining);
    }
}
