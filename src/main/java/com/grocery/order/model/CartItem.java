package com.grocery.order.model;

import com.grocery.common.FileHandler;

/**
 * Represents an item stored in a user's shopping cart.
 * File format: cartId|userId|itemId|quantity|addedDate
 */
public class CartItem {

    private String cartId;
    private String userId;
    private String itemId;
    private int quantity;
    private String addedDate;

    // Transient presentation fields (enriched via catalog lookup)
    private String itemName;
    private double unitPrice;
    private String unit;

    public CartItem() {
    }

    public CartItem(String cartId, String userId, String itemId, int quantity, String addedDate) {
        this.cartId = cartId;
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.addedDate = addedDate;
    }

    /** Serializes the entity to a pipe-delimited text record */
    public String toRecordLine() {
        return String.format("%s|%s|%s|%d|%s",
                FileHandler.clean(cartId),
                FileHandler.clean(userId),
                FileHandler.clean(itemId),
                quantity,
                FileHandler.clean(addedDate));
    }

    /** Parses a pipe-delimited text line into a CartItem */
    public static CartItem fromRecordLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            return null;
        }
        String cId = parts[0].trim();
        String uId = parts[1].trim();
        String iId = parts[2].trim();
        int qty = 1;
        try {
            qty = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException ignored) {}

        String date = parts.length > 4 ? parts[4].trim() : "";
        return new CartItem(cId, uId, iId, qty, date);
    }

    public double getLineTotal() {
        return Math.round(quantity * unitPrice * 100.0) / 100.0;
    }

    // Getters and Setters
    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public String getItemName() {
        return itemName != null ? itemName : itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getUnit() {
        return unit != null ? unit : "unit";
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
