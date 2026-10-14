package com.grocery.order.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.order.model.CartItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Accesses and persists CartItem records in data/carts.txt using FileHandler.
 */
@Repository
public class CartRepository {

    private static final String FILE_NAME = "carts.txt";

    /** Returns all cart records in the file */
    public List<CartItem> findAll() {
        List<String> lines = FileHandler.readLines(FILE_NAME);
        List<CartItem> items = new ArrayList<>();
        for (String line : lines) {
            CartItem item = CartItem.fromRecordLine(line);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /** Finds all cart items belonging to a given user */
    public List<CartItem> findByUserId(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(item -> userId.equalsIgnoreCase(item.getUserId()))
                .collect(Collectors.toList());
    }

    /** Finds a specific cart item by cartId */
    public Optional<CartItem> findById(String cartId) {
        if (cartId == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(item -> cartId.equalsIgnoreCase(item.getCartId()))
                .findFirst();
    }

    /** Finds if an item already exists in the user's cart */
    public Optional<CartItem> findByUserAndItem(String userId, String itemId) {
        if (userId == null || itemId == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(item -> userId.equalsIgnoreCase(item.getUserId()) && itemId.equalsIgnoreCase(item.getItemId()))
                .findFirst();
    }

    /**
     * Saves a CartItem. If cartId is null or blank, assigns a new CRT ID and appends.
     * Otherwise updates the existing line.
     */
    public synchronized CartItem save(CartItem cartItem) {
        List<CartItem> all = findAll();
        if (cartItem.getCartId() == null || cartItem.getCartId().isBlank()) {
            List<String> existingIds = all.stream().map(CartItem::getCartId).collect(Collectors.toList());
            String newId = IdGenerator.nextId("CRT", existingIds);
            cartItem.setCartId(newId);
            FileHandler.appendLine(FILE_NAME, cartItem.toRecordLine());
            return cartItem;
        }

        // Update existing record
        boolean found = false;
        List<String> updatedLines = new ArrayList<>();
        for (CartItem item : all) {
            if (item.getCartId().equalsIgnoreCase(cartItem.getCartId())) {
                updatedLines.add(cartItem.toRecordLine());
                found = true;
            } else {
                updatedLines.add(item.toRecordLine());
            }
        }

        if (!found) {
            updatedLines.add(cartItem.toRecordLine());
        }
        FileHandler.writeLines(FILE_NAME, updatedLines);
        return cartItem;
    }

    /** Deletes a single item by cartId */
    public synchronized void deleteById(String cartId) {
        if (cartId == null) return;
        List<CartItem> all = findAll();
        List<String> remaining = all.stream()
                .filter(item -> !cartId.equalsIgnoreCase(item.getCartId()))
                .map(CartItem::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(FILE_NAME, remaining);
    }

    /** Clears all cart items belonging to a user (used after checkout) */
    public synchronized void deleteByUserId(String userId) {
        if (userId == null) return;
        List<CartItem> all = findAll();
        List<String> remaining = all.stream()
                .filter(item -> !userId.equalsIgnoreCase(item.getUserId()))
                .map(CartItem::toRecordLine)
                .collect(Collectors.toList());
        FileHandler.writeLines(FILE_NAME, remaining);
    }
}
